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
from core.types import FrameSignals, FocusState
from core.config import Config
from detectors.absence_detector import AbsenceDetector
from detectors.drowsiness_detector import DrowsinessDetector
from detectors.phone_detector import PhoneDetector
from fusion.state_fuser import StateFuser
from scoring.focus_scorer import FocusScorer


async def ai_process_loop(room: rtc.Room, video_stream: rtc.VideoStream, queue: multiprocessing.Queue = None, participant_identity: str = ""):
    print("[INFO] AI Processing Loop Started", flush=True)
    
    # Initialize Logic
    extractor = FeatureExtractor() # Use original extractor
    abs_det = AbsenceDetector()
    drowsy_det = DrowsinessDetector()
    phone_det = PhoneDetector()
    fuser = StateFuser()
    scorer = FocusScorer()
    last_valid_event = "FOCUS" # [NEW] To hold state during UNKNOWN

    frame_count = 0
    last_sent_time = 0
    SEND_INTERVAL = 0.1 # Send data every 100ms

    async for frame in video_stream:
        start_time = time.time()
        frame_count += 1
        
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
        feats = extractor.process(img)

        # 2. Detectors
        sig_abs = abs_det.process(feats["face_detected"])
        sig_drowsy = drowsy_det.process(feats["face_detected"], feats["ear"], feats["pitch"])
        sig_phone = phone_det.process(feats["phone_conf"], feats["face_detected"], feats["pitch"], feats["hand_interaction"])
        
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

async def _send_data(room: rtc.Room, category: str, value, queue: multiprocessing.Queue = None, target_id: str = None):
    # 1. Send via LiveKit
    payload_dict = {"category": category, "value": value}
    if target_id:
        payload_dict["participantId"] = target_id
        
    payload = json.dumps(payload_dict)
    data = payload.encode('utf-8')
    await room.local_participant.publish_data(data, reliable=False)

    # 2. Queue(소켓)로 보낼 때
    if queue:
        try:
            # 프론트엔드 에러 해결의 핵심: participantId 추가
            queue_payload = {"eventType": category, "value": value}
            if target_id:
                queue_payload["participantId"] = target_id
            
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
            asyncio.create_task(ai_process_loop(room, video_stream, queue, participant.identity))

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
                     asyncio.create_task(ai_process_loop(room, video_stream, queue))
                
                # 2. 구독 안 됨 -> 구독 시도
                elif not publication.subscribed:
                    print(f"[INFO] Found unsubscribed video for {identity}. Subscribing...", flush=True)
                    publication.set_subscribed(True)
                    # 구독하면 자동으로 on_track_subscribed가 호출되므로 
                    # 여기서 ai_process_loop를 직접 실행할 필요는 없습니다.
        # ==============================================================================
        # [추가] 봇 인내심 기르기 (입장 후 30초 대기)
        # ==============================================================================
        # 처음 확인했을 때 아무도 없다면, 30초 동안은 사용자가 들어올 때까지 기다립니다.
        if len(room.remote_participants) == 0:
            print("[WAIT] No participants found. Waiting 30s for user to join...", flush=True)
            for i in range(30):
                if len(room.remote_participants) > 0:
                    print(f"[INFO] User detected! Starting analysis. (Waited {i}s)", flush=True)
                    break
                await asyncio.sleep(1)
            
            # 30초를 기다렸는데도 여전히 아무도 없으면 그때 종료합니다.
            if len(room.remote_participants) == 0:
                print("[INFO] Room empty for 30 seconds. Disconnecting.", flush=True)
                await room.disconnect()
                return
        # ==============================================================================            
        
        # Keep alive & Monitor
        empty_room_start = None
        
        while True:
            await asyncio.sleep(1)
            count = len(room.remote_participants)
            
            if count == 0:
                if empty_room_start is None:
                    empty_room_start = time.time()
                elif time.time() - empty_room_start > 1.0:
                    print(f"[INFO] Room empty for 1 second. Disconnecting...", flush=True)
                    break
            else:
                empty_room_start = None

            # Periodic check for debug
            if count > 0:
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
