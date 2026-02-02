import asyncio
import argparse
import cv2
import numpy as np
import json
import time
import multiprocessing
import livekit.api as api
import livekit.rtc as rtc




# Reuse imports from core modules
from run import FeatureExtractor
from core.types import FrameSignals, FocusState
from core.config import Config
from detectors.absence_detector import AbsenceDetector
from detectors.drowsiness_detector import DrowsinessDetector
from detectors.phone_detector import PhoneDetector
from fusion.state_fuser import StateFuser
from scoring.focus_scorer import FocusScorer


async def ai_process_loop(room: rtc.Room, video_stream: rtc.VideoStream, room_id: str, queue: multiprocessing.Queue = None, debug_visual: bool = False):
    print("[INFO] AI Processing Loop Started", flush=True)
    
    # Initialize Logic
    extractor = FeatureExtractor() 
    # extractor = FeatureExtractor() # Use original extractor (removed redundant comment)
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
        
        # [DEBUG] Check resolution for the first 50 frames (to see if it scales up)
        if frame_count <= 50:
            print(f"[DEBUG] Frame Resolution: {w}x{h}", flush=True)

        # 1. Feature Extraction
        feats = extractor.process(img)

        # 2. Detectors
        sig_abs = abs_det.process(feats["face_detected"])
        sig_drowsy = drowsy_det.process(feats["face_detected"], feats["ear"], feats["pitch"])
        sig_phone = phone_det.process(
            feats["phone_conf"], 
            feats["face_detected"], 
            feats["pitch"], 
            feats["hand_interaction"],
            feats["hand_near_face"]
        )
        
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
            await _send_data(room, event_type, value, queue)
            
            # Send SCORE Event (Separate)
            await _send_data(room, "SCORE", int(snap.score), queue)

            last_sent_time = current_time

        # [NEW] Optional Debug Visualization
        if debug_visual:
            display_frame = img.copy()
            # Draw overlay with state and score
            cv2.putText(display_frame, f"STATE: {last_valid_event}", (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (0, 255, 0), 2)
            cv2.putText(display_frame, f"SCORE: {int(snap.score)}", (10, 60), cv2.FONT_HERSHEY_SIMPLEX, 0.8, (255, 255, 0), 2)
            
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
    # 2. Send via Queue (to Frontend Socket)
    if queue:
        try:
            # Frontend Logic:
            # - Expects { eventType: "FOCUS", value: 0 }
            # - Expects { eventType: "SCORE", value: 85 }
            
            queue_payload = {"eventType": category, "value": value}
            queue.put(queue_payload)
            print(f"[DEBUG] Queue Put: {category} -> {value}") # Explicit print
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
    
    @room.on("track_subscribed")
    def on_track_subscribed(track: rtc.Track, publication: rtc.TrackPublication, participant: rtc.RemoteParticipant):
        if track.kind == rtc.TrackKind.KIND_VIDEO:
            print(f"[INFO] Subscribed to Video Track from {participant.identity}", flush=True)
            video_stream = rtc.VideoStream(track)
            asyncio.create_task(ai_process_loop(room, video_stream, room_id, queue, debug_visual))

    @room.on("data_received")
    def on_data_received(data: rtc.DataPacket):
        # [Fix] Updated signature for LiveKit SDK 1.0+
        # 'data' is now a DataPacket object
        # print(f"[DEBUG] Data received from {data.participant.identity}: {data.data.decode()}", flush=True)
        pass

    @room.on("participant_connected")
    def on_participant_connected(participant: rtc.RemoteParticipant):
        print(f"[DEBUG] Participant Connected: {participant.identity}", flush=True)

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
                     video_stream = rtc.VideoStream(publication.track)
                     asyncio.create_task(ai_process_loop(room, video_stream, room_id, queue, debug_visual))
        
        # Keep alive & Monitor
        empty_room_start = None
        
        while True:
            await asyncio.sleep(1)
            count = len(room.remote_participants)
            
            if count == 0:
                if empty_room_start is None:
                    empty_room_start = time.time()
                elif time.time() - empty_room_start > 60.0: # Keep alive for 60 seconds even if empty
                    print(f"[INFO] Room empty for 60 seconds. Disconnecting...", flush=True)
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

# Start bot if run as main script
if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--url", required=True, help="LiveKit URL")
    parser.add_argument("--token", required=True, help="LiveKit Token")
    args = parser.parse_args()
    
    asyncio.run(run_bot(args.url, args.token))
