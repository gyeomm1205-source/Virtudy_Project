import asyncio
import argparse
import cv2
import numpy as np
import json
import time
from livekit import api, rtc

# Reuse imports from core modules
from run import FeatureExtractor
from core.types import FrameSignals, FocusState
from core.config import Config
from detectors.absence_detector import AbsenceDetector
from detectors.drowsiness_detector import DrowsinessDetector
from detectors.phone_detector import PhoneDetector
from fusion.state_fuser import StateFuser
from scoring.focus_scorer import FocusScorer


async def ai_process_loop(room: rtc.Room, video_stream: rtc.VideoStream):
    print("[INFO] AI Processing Loop Started")
    
    # Initialize Logic
    extractor = FeatureExtractor() # Use original extractor
    abs_det = AbsenceDetector()
    drowsy_det = DrowsinessDetector()
    phone_det = PhoneDetector()
    fuser = StateFuser()
    scorer = FocusScorer()

    # Restoration of thresholds
    Config.PITCH_DROWSY_TH, Config.PITCH_PHONE_USE_TH = 0.45, 0.40
    Config.EAR_DROWSY_TH = 0.18

    frame_count = 0
    last_sent_time = 0
    SEND_INTERVAL = 0.1 # Send data every 100ms

    async for frame in video_stream:
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

        # 1. Feature Extraction
        feats = extractor.process(img)

        # 2. Detectors
        sig_abs = abs_det.process(feats["face_detected"])
        sig_drowsy = drowsy_det.process(feats["face_detected"], feats["ear"], feats["pitch"])
        sig_phone = phone_det.process(feats["phone_conf"], feats["face_detected"], feats["pitch"], feats["hand_interaction"])
        
        signals = FrameSignals(drowsy=sig_drowsy, absent=sig_abs, phone=sig_phone)
        decision = fuser.decide(signals)
        snap = scorer.update(decision.state)

        # 3. Prepare & Send Data Payload (Simplified)
        current_time = time.time()
        if current_time - last_sent_time >= SEND_INTERVAL:
            # STATUS MAPPING
            status_str = decision.state.name 
            if status_str == "DROWSY": status_str = "SLEEP"
            if status_str == "ABSENT": status_str = "AWAY"
            if status_str == "FOCUSED": status_str = "FOCUS"
            
            # Send STATUS
            await _send_data(room, "STATUS", status_str)
            print(f"[DEBUG] Sent STATUS: {status_str}, SCORE: {int(snap.score)}")
            
            # Send SCORE
            await _send_data(room, "SCORE", int(snap.score))

            last_sent_time = current_time

async def _send_data(room: rtc.Room, category: str, value):
    payload = json.dumps({"category": category, "value": value})
    data = payload.encode('utf-8')
    await room.local_participant.publish_data(data, reliable=False)

async def main(url: str, token: str):
    room = rtc.Room()
    
    @room.on("track_subscribed")
    def on_track_subscribed(track: rtc.Track, publication: rtc.TrackPublication, participant: rtc.RemoteParticipant):
        if track.kind == rtc.TrackKind.KIND_VIDEO:
            print(f"[INFO] Subscribed to Video Track from {participant.identity}")
            video_stream = rtc.VideoStream(track)
            asyncio.create_task(ai_process_loop(room, video_stream))

    print(f"[INFO] Connecting to LiveKit Room...")
    await room.connect(url, token)
    print(f"[INFO] Connected to {room.name}")

    # Keep alive
    try:
        while True:
            await asyncio.sleep(1)
    except KeyboardInterrupt:
        await room.disconnect()

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--url", required=True, help="LiveKit URL")
    parser.add_argument("--token", required=True, help="LiveKit Token")
    args = parser.parse_args()
    
    asyncio.run(main(args.url, args.token))
