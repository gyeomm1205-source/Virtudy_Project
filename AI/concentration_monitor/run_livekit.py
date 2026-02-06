import asyncio
import argparse
import cv2
import numpy as np
import time
import multiprocessing
import livekit.api as api
import livekit.rtc as rtc




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

def _should_log(last_times: dict, key: str, interval_sec: float, now: float = None) -> bool:
    now = now or time.time()
    last = last_times.get(key, 0.0)
    if now - last >= interval_sec:
        last_times[key] = now
        return True
    return False


<<<<<<< HEAD
async def ai_process_loop(
    room: rtc.Room,
    video_stream: rtc.VideoStream,
    queue: multiprocessing.Queue = None,
    participant_identity: str = "",
    session_id: str = None,
    initial_score: float = 100.0,
    score_cache: dict = None,
):
    print(f"[INFO] AI Processing Loop Started (SessionID: {session_id})", flush=True)
=======
async def ai_process_loop(room: rtc.Room, video_stream: rtc.VideoStream, room_id: str, queue: multiprocessing.Queue = None, debug_visual: bool = False):
    print("[INFO] AI Processing Loop Started", flush=True)
>>>>>>> 1d12e087b06e8ccc4de00953fd963920a5f14c00
    
    # Initialize Logic
    extractor = FeatureExtractor() 
    # extractor = FeatureExtractor() # Use original extractor (removed redundant comment)
    abs_det = AbsenceDetector()
    drowsy_det = DrowsinessDetector()
    phone_det = PhoneDetector()
    fuser = StateFuser()
    scorer = FocusScorer(initial_score=initial_score)

    # 카프카 로거 초기화
    # [FIX] Use session_id if provided, else fallback to room.name (though likely to fail in backend)
    logger_id = session_id if session_id else room.name
    kafka_logger = KafkaLogger(session_id=logger_id)
    
    last_valid_event = "FOCUS" # [NEW] To hold state during UNKNOWN
    last_phone_signal = PhoneSignal(phone_present=False)
    log_times = {}
    last_state_logged = None

    frame_count = 0
<<<<<<< HEAD
    # Sampling controls (30 FPS 기준)
    DROWSY_EVERY_N_FRAMES = 5   # 약 6 FPS (부하 완화)
    PHONE_EVERY_N_FRAMES = 150  # 약 5초마다 (부하 완화)
    last_status_sent_time = 0
    last_score_sent_time = 0
    STATUS_SEND_INTERVAL = 0.1 # Status every 100ms
    SCORE_SEND_INTERVAL = 1.0  # Score every 1s
=======
    last_sent_time = 0
    last_ear = None  # [NEW] For tracking EAR changes
    SEND_INTERVAL = 0.1 # Send data every 100ms
    quality_ready = False
    min_width, min_height = 1280, 720
>>>>>>> 1d12e087b06e8ccc4de00953fd963920a5f14c00

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
<<<<<<< HEAD
        log_now = time.time()
=======
        proc_img = img
        proc_scale_x = proc_scale_y = 1.0
        if Config.PROCESSING_MAX_SIZE and max(w, h) > Config.PROCESSING_MAX_SIZE:
            scale = Config.PROCESSING_MAX_SIZE / max(w, h)
            new_w, new_h = int(w * scale), int(h * scale)
            proc_img = cv2.resize(img, (new_w, new_h), interpolation=cv2.INTER_AREA)
            proc_scale_x = w / new_w
            proc_scale_y = h / new_h
>>>>>>> 1d12e087b06e8ccc4de00953fd963920a5f14c00
        
        # [DEBUG] Check resolution for the first 50 frames (to see if it scales up)
        if frame_count <= 50:
            print(f"[DEBUG] Frame Resolution: {w}x{h}", flush=True)

        # Wait for high quality stream before running detection/calibration
        if not quality_ready:
            if w >= min_width and h >= min_height:
                quality_ready = True
                print(f"[INFO] High quality confirmed: {w}x{h}. Starting AI processing.", flush=True)
            else:
                if frame_count % 10 == 0:
                    print(f"[INFO] Waiting for {min_width}x{min_height}... current {w}x{h}", flush=True)
                continue

        # 1. Feature Extraction
<<<<<<< HEAD
        feats = extractor.process(
            img,
            detect_hands=do_phone,
            detect_phone=do_phone,
        )

        # 2. Detectors
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
        sig_abs = abs_det.process(feats["face_present"], sig_phone.phone_present)
        
        # [DEBUG] Print raw values to debug detection failure
        # [DEBUG] Print raw values to debug detection failure
        if feats['face_detected']:
             ear_val = feats['ear'] if feats['ear'] is not None else 0.0
             pitch_val = feats['pitch'] if feats['pitch'] is not None else 0.0
             phone_val = feats['phone_conf']
             
             # Calculate FPS/Latency
             process_time = time.time() - start_time
             fps = 1.0 / process_time if process_time > 0 else 0
             
             # Print THRESHOLD to confirm it updated (rate-limited)
             if _should_log(log_times, "raw", 1.0, log_now):
                 print(f"[DEBUG] EAR: {ear_val:.3f} (Th: {Config.EAR_DROWSY_TH}), Pitch: {pitch_val:.3f} (Th: {Config.PITCH_PHONE_USE_TH}), Phone: {phone_val:.3f}, FPS: {fps:.1f}", flush=True)
        else:
             if _should_log(log_times, "noface", 1.0, log_now):
                 print(f"[DEBUG] NO FACE DETECTED", flush=True)
=======
        feats = extractor.process(proc_img)

        # 2. Detectors
        sig_abs = abs_det.process(feats["face_detected"])
        sig_drowsy = drowsy_det.process(
            feats["face_detected"],
            feats["ear"],
            feats["pitch"],
            feats.get("face_updated", True),
        )
        sig_phone = phone_det.process(
            feats["phone_conf"], 
            feats["is_cell_phone"],
            feats["face_detected"], 
            feats["pitch"], 
            feats["hand_interaction"],
            feats["hand_near_face"],
            feats.get("phone_near_face", False),
            feats.get("phone_area_ratio", 0.0)
        )
        
        # [DEBUG] Print raw values to debug detection failure
        if feats["face_detected"]:
            ear_val = feats["ear"] if feats["ear"] is not None else 0.0
            pitch_val = feats["pitch"] if feats["pitch"] is not None else 0.0
            phone_val = feats["phone_conf"]

            # Calculate FPS/Latency
            process_time = time.time() - start_time
            fps = 1.0 / process_time if process_time > 0 else 0.0

            # Print threshold to confirm it updated (use adaptive threshold)
            current_th = sig_drowsy.current_threshold
            eyes_closed_status = "CLOSED" if ear_val < current_th else "OPEN"

            # Highlight EAR changes
            if last_ear is None:
                last_ear = ear_val
            ear_change = abs(ear_val - last_ear)

            # Print always if eyes just closed/opened (big change), otherwise print every 10 frames
            if ear_change > 0.05 or frame_count % 10 == 0:
                print(
                    f"[DEBUG] {eyes_closed_status} | EAR: {ear_val:.3f} (dEAR {ear_change:+.3f}, Th: {current_th:.3f}), "
                    f"Pitch: {pitch_val:.3f} (Th: {Config.PITCH_PHONE_USE_TH}), "
                    f"Phone: {phone_val:.3f}, Hand-Int: {feats['hand_interaction']}, "
                    f"Hand-Near: {feats['hand_near_face']}, Phone-Near: {feats.get('phone_near_face', False)}, "
                    f"Phone-Area: {feats.get('phone_area_ratio', 0.0):.3f}, "
                    f"FPS: {fps:.1f}",
                    flush=True,
                )

            last_ear = ear_val
        else:
            print("[DEBUG] NO FACE DETECTED", flush=True)
>>>>>>> 1d12e087b06e8ccc4de00953fd963920a5f14c00

        signals = FrameSignals(drowsy=sig_drowsy, absent=sig_abs, phone=sig_phone)
        decision = fuser.decide(signals)
        snap = scorer.update(decision.state)
<<<<<<< HEAD
        if score_cache is not None:
            score_cache[participant_identity] = snap.score

        # [DEBUG] State change log (rate-limited)
        if decision.state != last_state_logged and _should_log(log_times, "state", 1.0, log_now):
            print(f"[STATE] {decision.state.name} | Conf: {decision.confidence:.2f} | Reason: {decision.reason}", flush=True)
            last_state_logged = decision.state

        # [KAFKA] Log State
        kafka_logger.log_state(decision.state)
=======
        
        # [DEBUG_STATE] High visibility log
        if frame_count % 3 == 0:  # More frequent for debugging
            log_msg = f"[STATE] {decision.state.name} | Conf: {decision.confidence:.2f} | Reason: {decision.reason}"
            if sig_phone.phone_present and not sig_phone.is_cell_phone:
                log_msg += f" (Note: Phantom {feats.get('debug', {}).get('detected_classes', 'object')} Ignored)"
            print(log_msg, flush=True)

        if frame_count % 10 == 0:
            ear_dbg = f"{sig_drowsy.ear:.3f}" if sig_drowsy.ear is not None else "None"
            th_dbg = f"{sig_drowsy.current_threshold:.3f}" if sig_drowsy.current_threshold is not None else "None"
            print(
                f"[DEBUG_RAW] EAR={ear_dbg} Th={th_dbg} | PhoneInUse={sig_phone.phone_in_use} "
                f"Score={sig_drowsy.drowsy_score}",
                flush=True,
            )
>>>>>>> 1d12e087b06e8ccc4de00953fd963920a5f14c00

        # 3. Prepare & Send Data Payload (Simplified)
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

        current_time = time.time()
        if current_time - last_status_sent_time >= STATUS_SEND_INTERVAL:
            # Send STATUS Event (Strict JSON Spec)
            # We pass 'event_type' as category, but _send_data will now ensure it maps to 'eventType'
            await _send_data(room, event_type, value, queue, participant_identity)
            last_status_sent_time = current_time

        if current_time - last_score_sent_time >= SCORE_SEND_INTERVAL:
            # Send SCORE Event (Separate, lower rate)
            await _send_data(room, "SCORE", int(snap.score), queue, participant_identity)
            last_score_sent_time = current_time

<<<<<<< HEAD
    # [KAFKA] Close Logger
    kafka_logger.close()

async def _send_data(room: rtc.Room, category: str, value, queue: multiprocessing.Queue = None, participant_identity: str = ""):
    # [Fix] Disable AI bot LiveKit publish to avoid cross-user mixups.
    # State updates are sent only via the AI WebSocket queue.

    # 2. Queue(소켓)로 보낼 때
=======
        # [NEW] Optional Debug Visualization
        if debug_visual:
            display_frame = img.copy()
            # Draw overlay with state and score
            cv2.putText(display_frame, f"STATE: {last_valid_event}", (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 255, 0), 2)
            cv2.putText(display_frame, f"SCORE: {int(snap.score)}", (10, 60), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (255, 255, 0), 2)
            
            # Draw detection overlays
            dbg = feats.get("debug", {})
            phone_box = dbg.get("phone_box")
            hand_centers = dbg.get("hand_centers", [])
            face_center = dbg.get("face_center")
            if phone_box:
                x1, y1, x2, y2 = phone_box
                x1, y1 = int(x1 * proc_scale_x), int(y1 * proc_scale_y)
                x2, y2 = int(x2 * proc_scale_x), int(y2 * proc_scale_y)
                cv2.rectangle(display_frame, (x1, y1), (x2, y2), (0, 165, 255), 2)
                cv2.putText(display_frame, "phone", (x1, max(15, y1 - 5)), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 165, 255), 2)
            for hx, hy in hand_centers:
                cx, cy = int(hx * proc_scale_x), int(hy * proc_scale_y)
                cv2.circle(display_frame, (cx, cy), 6, (255, 0, 0), -1)
            if face_center:
                fx, fy = int(face_center[0] * proc_scale_x), int(face_center[1] * proc_scale_y)
                cv2.circle(display_frame, (fx, fy), 6, (0, 255, 0), -1)

            # Show key phone signals
            phone_conf = feats.get("phone_conf", 0.0)
            phone_near = feats.get("phone_near_face", False)
            hand_int = feats.get("hand_interaction", False)
            cv2.putText(
                display_frame,
                f"Phone: {phone_conf:.2f} Near:{phone_near} Hand:{hand_int}",
                (10, 120),
                cv2.FONT_HERSHEY_SIMPLEX,
                0.6,
                (255, 255, 255),
                2,
            )
            if proc_scale_x != 1.0 or proc_scale_y != 1.0:
                proc_w = int(w / proc_scale_x)
                proc_h = int(h / proc_scale_y)
                cv2.putText(display_frame, f"PROC: {proc_w}x{proc_h}", (10, 150), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (255, 255, 255), 2)

            # Show FPS
            process_time = time.time() - start_time
            fps = 1.0 / process_time if process_time > 0 else 0
            cv2.putText(display_frame, f"FPS: {fps:.1f}", (10, 90), cv2.FONT_HERSHEY_SIMPLEX, 0.6, (0, 255, 255), 2)

            cv2.imshow(f"LiveKit AI Debug - {room_id}", display_frame)
            if cv2.waitKey(1) & 0xFF == ord('q'):
                break

async def _send_data(room: rtc.Room, category: str, value, queue: multiprocessing.Queue = None):
    # 1. Send via LiveKit
    try:
        if room.isconnected():
            payload = json.dumps({"category": category, "value": value})
            data = payload.encode('utf-8')
            await room.local_participant.publish_data(data, reliable=False)
    except Exception as e:
        print(f"[DEBUG] Failed to publish {category} (Room likely closed): {e}")


    # 2. Send via Queue (to Frontend Socket)
>>>>>>> 1d12e087b06e8ccc4de00953fd963920a5f14c00
    if queue:
        try:
            # 프론트엔드 에러 해결의 핵심: participantId 추가
            queue_payload = {"eventType": category, "value": value, "participantId": participant_identity}
            
            queue.put(queue_payload)
            # print(f"[DEBUG] Queue Put: {category} -> {value} (User: {target_id})") 
        except Exception as e:
            print(f"[WARN] Queue put failed: {e}")

async def request_high_quality(publication: rtc.RemoteTrackPublication):
    """
    Manually request high quality and set dimensions for a remote track.
    Repeatedly call this to ensure the SFU processes the request as the track stabilizes.
    """
    try:
        from livekit.rtc._proto import ffi_pb2 as proto_ffi
        from livekit.rtc._proto import track_publication_pb2 as proto_track_publication
        from livekit.rtc._ffi_client import FfiClient

        for i in range(3): # Try 3 times with slight delay
            # 1. Set Quality to HIGH
            req = proto_ffi.FfiRequest()
            req.set_remote_track_publication_quality.track_publication_handle = publication._ffi_handle.handle
            req.set_remote_track_publication_quality.quality = proto_track_publication.VIDEO_QUALITY_HIGH
            FfiClient.instance.request(req)
            
            # 2. Update Dimension (Force 720p layer)
            req = proto_ffi.FfiRequest()
            req.update_remote_track_publication_dimension.track_publication_handle = publication._ffi_handle.handle
            req.update_remote_track_publication_dimension.width = 1280
            req.update_remote_track_publication_dimension.height = 720
            FfiClient.instance.request(req)
            
            if i == 0:
                print(f"[DEBUG] Requested HIGH quality (1280x720) for {publication.sid}", flush=True)
            await asyncio.sleep(1.0) # Wait for SFU to switch layers
            
    except Exception as e:
        print(f"[WARN] Failed to request high quality: {e}", flush=True)

async def run_bot(url: str, token: str, room_id: str = "DEBUG_SESSION", queue: multiprocessing.Queue = None, debug_visual: bool = False):
    print(f"[INFO] Connecting to room: {room_id}", flush=True)
    room = rtc.Room()
    track_tasks: dict[str, asyncio.Task] = {}
    participant_tracks: dict[str, set[str]] = {}
    score_cache: dict[str, float] = {}

    def _start_video_task(track: rtc.Track, participant: rtc.RemoteParticipant):
        track_sid = getattr(track, "sid", None)
        if not track_sid:
            return
        if track_sid in track_tasks:
            return
        video_stream = rtc.VideoStream(track)
        session_id = participant.metadata
        initial_score = score_cache.get(participant.identity, 100.0)
        task = asyncio.create_task(
            ai_process_loop(
                room,
                video_stream,
                queue,
                participant.identity,
                session_id,
                initial_score,
                score_cache,
            )
        )
        track_tasks[track_sid] = task
        participant_tracks.setdefault(participant.identity, set()).add(track_sid)

    def _stop_track(track_sid: str):
        task = track_tasks.pop(track_sid, None)
        if task:
            task.cancel()

    @room.on("track_subscribed")
    def on_track_subscribed(track: rtc.Track, publication: rtc.TrackPublication, participant: rtc.RemoteParticipant):
        if track.kind == rtc.TrackKind.KIND_VIDEO:
<<<<<<< HEAD
            print(f"[INFO] Subscribed to Video Track from {participant.identity}", flush=True)
            # [Fix] Request High Quality Video for AI Analysis
            # publication.set_video_quality(rtc.VideoQuality.HIGH) # Unsupported in v1.0.23
            _start_video_task(track, participant)
=======
            print(f"[INFO] Subscribed to video track from {participant.identity}", flush=True)
            # [Add] Request HIGH quality immediately
            asyncio.create_task(request_high_quality(publication))
            video_stream = rtc.VideoStream(track)
            asyncio.create_task(ai_process_loop(room, video_stream, room_id, queue, debug_visual))
>>>>>>> 1d12e087b06e8ccc4de00953fd963920a5f14c00

    @room.on("data_received")
    def on_data_received(data: rtc.DataPacket):
        # [Fix] Updated signature for LiveKit SDK 1.0+
        # 'data' is now a DataPacket object
        # print(f"[DEBUG] Data received from {data.participant.identity}: {data.data.decode()}", flush=True)
        pass

    @room.on("track_unsubscribed")
    def on_track_unsubscribed(track: rtc.Track, publication: rtc.TrackPublication, participant: rtc.RemoteParticipant):
        track_sid = getattr(track, "sid", None)
        if track_sid:
            print(f"[INFO] Track Unsubscribed: {participant.identity} ({track_sid})", flush=True)
            _stop_track(track_sid)
            participant_tracks.get(participant.identity, set()).discard(track_sid)

    @room.on("participant_connected")
    def on_participant_connected(participant: rtc.RemoteParticipant):
        print(f"[DEBUG] Participant Connected: {participant.identity}", flush=True)

    @room.on("participant_disconnected")
    def on_participant_disconnected(participant: rtc.RemoteParticipant):
        print(f"[INFO] Participant Disconnected: {participant.identity}", flush=True)
        track_ids = participant_tracks.pop(participant.identity, set())
        for track_sid in track_ids:
            _stop_track(track_sid)

    print(f"[INFO] Connecting to LiveKit Room...", flush=True)
    try:
        # [Fix] Use valid RoomOptions for SDK 1.0+
        options = rtc.RoomOptions(auto_subscribe=True)
        await room.connect(url, token, options=options)
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
<<<<<<< HEAD
                     # [Fix] Request High Quality Video for AI Analysis
                     publication.set_video_quality(rtc.VideoQuality.HIGH)
                     _start_video_task(publication.track, participant)
                     pass
                
                # 2. 구독 안 됨 -> 구독 시도
                elif not publication.subscribed:
                    print(f"[INFO] Found unsubscribed video for {identity}. Subscribing...", flush=True)
                    publication.set_subscribed(True)
                    # 구독하면 자동으로 on_track_subscribed가 호출되므로 
                    # 여기서 ai_process_loop를 직접 실행할 필요는 없습니다.
=======
                     # [Add] Request HIGH quality for existing tracks
                     asyncio.create_task(request_high_quality(publication))
                     video_stream = rtc.VideoStream(publication.track)
                     asyncio.create_task(ai_process_loop(room, video_stream, room_id, queue, debug_visual))
>>>>>>> 1d12e087b06e8ccc4de00953fd963920a5f14c00
        
        # Keep alive & Monitor
        # Keep alive & Monitor
        empty_room_start = None
        
        # [수정 1] 대기 시간을 30초로 늘림 (프론트엔드 연결 지연 대비)
        MAX_WAIT_SECONDS = 30.0
        last_room_log_time = 0.0
        last_room_count = None
        while True:
            await asyncio.sleep(1) # 1초 대기
            
            # 1. 방에 사람 있는지 확인
            count = len(room.remote_participants)
            if count == 0:
                if empty_room_start is None:
                    empty_room_start = time.time()
<<<<<<< HEAD
                elif time.time() - empty_room_start > MAX_WAIT_SECONDS:
                    print(f"[INFO] Room empty for 1 second. Disconnecting...", flush=True)
=======
                elif time.time() - empty_room_start > 60.0: # Keep alive for 60 seconds even if empty
                    print(f"[INFO] Room empty for 60 seconds. Disconnecting...", flush=True)
>>>>>>> 1d12e087b06e8ccc4de00953fd963920a5f14c00
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

            # Periodic check for debug (rate-limited)
            if count > 0:
                 now = time.time()
                 if count != last_room_count or (now - last_room_log_time) >= 10.0:
                     print(f"[DEBUG] Room Status: {count} participants connected.", flush=True)
                     last_room_log_time = now
                     last_room_count = count
    except Exception as e:
        print(f"[ERROR] Connection failed: {e}")
    finally:
        for task in list(track_tasks.values()):
            task.cancel()
        track_tasks.clear()
        await room.disconnect()

# Start bot if run as main script
if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--url", required=True, help="LiveKit URL")
    parser.add_argument("--token", required=True, help="LiveKit Token")
    args = parser.parse_args()
    
    asyncio.run(run_bot(args.url, args.token))
