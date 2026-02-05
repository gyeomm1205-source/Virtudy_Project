import asyncio
import argparse
import cv2
import numpy as np
import json
import time
import multiprocessing
import livekit

import livekit
import livekit.api as api
import livekit.rtc as rtc




# Reuse imports from core modules
# Reuse imports from core modules
from core.feature_extractor import FeatureExtractor
from core.types import FrameSignals, FocusState, PhoneSignal
from core.config import Config
from detectors.absence_detector import AbsenceDetector
from detectors.drowsiness_detector import DrowsinessDetector
from detectors.phone_detector import PhoneDetector
from fusion.state_fuser import StateFuser
from scoring.focus_scorer import FocusScorer
from utils.kafka_logger import KafkaLogger


async def ai_process_loop(room: rtc.Room, video_stream: rtc.VideoStream, queue: multiprocessing.Queue = None, participant_identity: str = "", session_id: str = None):
    print(f"[INFO] AI Processing Loop Started (SessionID: {session_id})", flush=True)
    
    # Initialize Logic
    extractor = FeatureExtractor() # Use original extractor
    abs_det = AbsenceDetector()
    drowsy_det = DrowsinessDetector()
    phone_det = PhoneDetector()
    fuser = StateFuser()
    scorer = FocusScorer()

    # 카프카 로거 초기화
    # [FIX] Use session_id if provided, else fallback to room.name (though likely to fail in backend)
    logger_id = session_id if session_id else room.name
    kafka_logger = KafkaLogger(session_id=logger_id)
    
    last_valid_event = "FOCUS" # [NEW] To hold state during UNKNOWN
    last_phone_signal = PhoneSignal(phone_present=False)

    frame_count = 0
    # Sampling controls (30 FPS 기준)
    DROWSY_EVERY_N_FRAMES = 3   # 약 10 FPS (졸음 실시간성)
    PHONE_EVERY_N_FRAMES = 30   # 약 2.5초 (폰 감지 지연 허용)
    last_sent_time = 0
    SEND_INTERVAL = 0.1 # Send data every 100ms

    async for frame in video_stream:
        start_time = time.time()
        frame_count += 1
        
        do_drowsy = (frame_count % DROWSY_EVERY_N_FRAMES == 0)
        do_phone = (frame_count % PHONE_EVERY_N_FRAMES == 0)
        if not do_drowsy and not do_phone:
            continue
        # Convert LiveKit VideoFrame to CV2 image
        # frame is VideoFrameEvent, so we need frame.frame
        real_frame = frame.frame
        
        try:
            # 1. Convert to RGBA buffer
            rgba_buffer = real_frame.convert(rtc.VideoBufferType.RGBA)
            # 2. Get raw data as bytes
            raw_data = rgba_buffer.data
            # 3. Create numpy array
            arr = np.frombuffer(raw_data, dtype=np.uint8)
            # 4. Reshape (Height, Width, 4 channels)
            img = arr.reshape((real_frame.height, real_frame.width, 4))
            # 5. Convert RGBA to BGR for OpenCV
            img = cv2.cvtColor(img, cv2.COLOR_RGBA2BGR)
        except Exception as e:
            print(f"[ERROR] Frame conversion failed: {e}")
            continue
        
        if img is None: continue
        
        # Flip for processing 
        img = cv2.flip(img, 1)
        h, w, _ = img.shape
        
        # [DEBUG] Check resolution immediately
        if frame_count <= 10:
            print(f"[DEBUG] Frame Resolution: {w}x{h}", flush=True)

        # 1. Feature Extraction
        feats = extractor.process(
            img,
            detect_hands=do_phone,
            detect_phone=do_phone,
        )

        # 2. Detectors
        sig_abs = abs_det.process(feats["face_detected"])
        sig_drowsy = drowsy_det.process(feats["face_detected"], feats["ear"], feats["pitch"])
        if do_phone:
            sig_phone = phone_det.process(
                feats["phone_conf"],
                feats["face_detected"],
                feats["pitch"],
                feats["hand_interaction"],
            )
            last_phone_signal = sig_phone
        else:
            sig_phone = last_phone_signal
        
        # [DEBUG] Print raw values to debug detection failure
        # [DEBUG] Print raw values to debug detection failure
        if feats['face_detected']:
             ear_val = feats['ear'] if feats['ear'] is not None else 0.0
             pitch_val = feats['pitch'] if feats['pitch'] is not None else 0.0
             phone_val = feats['phone_conf']
             
             # Calculate FPS/Latency
             process_time = time.time() - start_time
             fps = 1.0 / process_time if process_time > 0 else 0
             
             # Print THRESHOLD to confirm it updated
             print(f"[DEBUG] EAR: {ear_val:.3f} (Th: {Config.EAR_DROWSY_TH}), Pitch: {pitch_val:.3f} (Th: {Config.PITCH_PHONE_USE_TH}), Phone: {phone_val:.3f}, FPS: {fps:.1f}", flush=True)
        else:
             print(f"[DEBUG] NO FACE DETECTED", flush=True)

        signals = FrameSignals(drowsy=sig_drowsy, absent=sig_abs, phone=sig_phone)
        decision = fuser.decide(signals)
        snap = scorer.update(decision.state)

        # [DEBUG] Decision Trace - Check if PHONE is really decided
        if signals.phone.phone_in_use or decision.state == FocusState.PHONE:
             print(f"[DEBUG-DECISION] Final: {decision.state.name}, PhoneInUse: {signals.phone.phone_in_use}", flush=True)

        # [KAFKA] Log State
        kafka_logger.log_state(decision.state)

        # 3. Prepare & Send Data Payload (Simplified)
        current_time = time.time()
        if current_time - last_sent_time >= SEND_INTERVAL:
            # STATUS MAPPING
            # Frontend Spec: 
            # "FOCUS" : 0
            # "SLEEP" : 1 (Drowsy)
            # "PHONE" : 1
            # "AWAY" : 1 (Absent/Empty)
            
            raw_state = decision.state.name # FOCUSED, DROWSY, ABSENT, PHONE, UNKNOWN
            
            # [NEW] Hold Last Valid State
            if raw_state != "UNKNOWN":
                event_type = raw_state
                if raw_state == "FOCUSED": event_type = "FOCUS"
                elif raw_state == "DROWSY": event_type = "SLEEP"
                elif raw_state == "ABSENT": event_type = "AWAY"
                last_valid_event = event_type
            else:
                event_type = last_valid_event # Maintain previous
            
            # Value Logic
            value = 0 if event_type == "FOCUS" else 1

            # Send STATUS Event (Strict JSON Spec)
            # We pass 'event_type' as category, but _send_data will now ensure it maps to 'eventType'
            await _send_data(room, event_type, value, queue, participant_identity)
            
            # Send SCORE Event (Separate)
            await _send_data(room, "SCORE", int(snap.score), queue, participant_identity)

            last_sent_time = current_time

    # [KAFKA] Close Logger
    kafka_logger.close()

async def _send_data(room: rtc.Room, category: str, value, queue: multiprocessing.Queue = None, participant_identity: str = ""):
    


    payload_dict = {"category": category, "value": value}
    
    payload = json.dumps(payload_dict)
    data = payload.encode('utf-8')
    await room.local_participant.publish_data(data, reliable=False)

    # 2. Queue(소켓)로 보낼 때
    if queue:
        try:
            # 프론트엔드 에러 해결의 핵심: participantId 추가
            queue_payload = {"eventType": category, "value": value, "participantId": participant_identity}
            
            queue.put(queue_payload)
            # print(f"[DEBUG] Queue Put: {category} -> {value} (User: {target_id})") 
        except Exception as e:
            print(f"[WARN] Queue put failed: {e}")

async def run_bot(url: str, token: str, queue: multiprocessing.Queue = None):
    print(f"[DEBUG] run_bot started! Connecting to: {url}", flush=True)
    room = rtc.Room()
    
    @room.on("track_subscribed")
    def on_track_subscribed(track: rtc.Track, publication: rtc.TrackPublication, participant: rtc.RemoteParticipant):
        if track.kind == rtc.TrackKind.KIND_VIDEO:
            print(f"[INFO] Subscribed to Video Track from {participant.identity}", flush=True)
            # [Fix] Request High Quality Video for AI Analysis
            # publication.set_video_quality(rtc.VideoQuality.HIGH) # Unsupported in v1.0.23
            video_stream = rtc.VideoStream(track)
            session_id = participant.metadata
            asyncio.create_task(ai_process_loop(room, video_stream, queue, participant.identity, session_id))

    @room.on("track_published")
    def on_track_published(publication: rtc.TrackPublication, participant: rtc.RemoteParticipant):
        print(f"[DEBUG] Track Published: {publication.kind} from {participant.identity}", flush=True)

    @room.on("participant_connected")
    def on_participant_connected(participant: rtc.RemoteParticipant):
        print(f"[DEBUG] Participant Connected: {participant.identity}", flush=True)

    print(f"[INFO] Connecting to LiveKit Room...", flush=True)
    try:
        await room.connect(url, token)
        print(f"[INFO] Connected to {room.name}", flush=True)

        # [CRITICAL Fix] Check for EXISTING participants/tracks
        # If user is already in room, 'track_published' might not fire, but they are in room.remote_participants
        print(f"[DEBUG] Checking for existing participants... (Count: {len(room.remote_participants)})", flush=True)
        
        for identity, participant in room.remote_participants.items():
            print(f"[DEBUG] Found existing participant: {identity}", flush=True)
            for track_sid, publication in participant.track_publications.items():
                print(f"[DEBUG] Found existing track: {publication.kind} (Subscribed: {publication.subscribed})", flush=True)
                # If already subscribed but logic didn't trigger (unlikely but safe)
                if publication.subscribed and publication.track and publication.kind == rtc.TrackKind.KIND_VIDEO:
                     print(f"[INFO] Found existing subscribed video, starting loop for {identity}", flush=True)
                     # [Fix] Request High Quality Video for AI Analysis
                     publication.set_video_quality(rtc.VideoQuality.HIGH)
                     video_stream = rtc.VideoStream(publication.track)
                     session_id = participant.metadata
                     asyncio.create_task(ai_process_loop(room, video_stream, queue, identity, session_id))
                     pass
                
                # 2. 구독 안 됨 -> 구독 시도
                elif not publication.subscribed:
                    print(f"[INFO] Found unsubscribed video for {identity}. Subscribing...", flush=True)
                    publication.set_subscribed(True)
                    # 구독하면 자동으로 on_track_subscribed가 호출되므로 
                    # 여기서 ai_process_loop를 직접 실행할 필요는 없습니다.
        
        # Keep alive & Monitor
        # Keep alive & Monitor
        empty_room_start = None
        
        # [수정 1] 대기 시간을 30초로 늘림 (프론트엔드 연결 지연 대비)
        MAX_WAIT_SECONDS = 30.0
        while True:
            await asyncio.sleep(1) # 1초 대기
            
            # 1. 방에 사람 있는지 확인
            count = len(room.remote_participants)
            if count == 0:
                if empty_room_start is None:
                    empty_room_start = time.time()
                elif time.time() - empty_room_start > MAX_WAIT_SECONDS:
                    print(f"[INFO] Room empty for 1 second. Disconnecting...", flush=True)
                    break
            else:
                if empty_room_start is not None:
                     print(f"[INFO] Participant detected! Timer reset.", flush=True)
                empty_room_start = None

            # 2. [추가된 기능] 루프 돌 때마다 비디오 트랙 다시 확인 (안전장치)
            for identity, participant in room.remote_participants.items():
                for track_sid, publication in participant.track_publications.items():
                    # 비디오 트랙이고, 아직 구독 안 했으면 -> 구독 시도
                    if publication.kind == rtc.TrackKind.KIND_VIDEO and not publication.subscribed:
                         print(f"[INFO] Found unsubscribed video track in loop for {identity}. Subscribing...", flush=True)
                         publication.set_subscribed(True)
                    
                    # 이미 구독됐는데 AI가 안 돌고 있는 경우는?
                    # (여기서 처리하기보단 on_track_subscribed가 해결해줍니다)

            # Periodic check for debug
            if count > 0:
                 # 로그가 너무 많이 쌓이면 보기 힘드므로 5초에 한 번만 출력하거나 그대로 둠
                 print(f"[DEBUG] Room Status: {count} participants connected.", flush=True)
    except Exception as e:
        print(f"[ERROR] Connection failed: {e}")
    finally:
        await room.disconnect()

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--url", required=True, help="LiveKit URL")
    parser.add_argument("--token", required=True, help="LiveKit Token")
    args = parser.parse_args()
    
    asyncio.run(run_bot(args.url, args.token))
