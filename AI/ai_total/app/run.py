import sys
import time
import cv2
import numpy as np
import os
import argparse

print("[INFO] Loading System Libraries...")
import mediapipe as mp
import mediapipe.python.solutions
from ultralytics import YOLO

# 1. 로직 모듈 임포트 (기존 로직 그대로 사용)
print("[INFO] Loading Logic Modules...", flush=True)
try:
    from core.types import FrameSignals, FocusDecision, FocusState
    from core.config import Config
    from detectors.absence_detector import AbsenceDetector
    from detectors.drowsiness_detector import DrowsinessDetector
    from detectors.phone_detector import PhoneDetector
    from fusion.state_fuser import StateFuser
    from scoring.focus_scorer import FocusScorer
    from utils.csv_logger import CSVLogger # CSV 기록용
    from utils.kafka_logger import KafkaLogger # Kafka 전송용
    print("[INFO] Logic Modules Loaded.", flush=True)
except Exception as e:
    print(f"[ERROR] Failed to load logic modules: {e}", flush=True)
    sys.exit(1)

# ==========================================
# Feature Extraction Helpers
# ==========================================
from core.feature_extractor import FeatureExtractor

# ==========================================
# UI Helpers (원래 UI 디자인)
# ==========================================
def draw_ui(frame, decision, snap, feats, signals):
    state = decision.state
    color_map = {
        FocusState.FOCUSED: (0, 255, 0), FocusState.DROWSY: (0, 0, 255),
        FocusState.PHONE: (0, 255, 255), FocusState.ABSENT: (255, 0, 0), FocusState.UNKNOWN: (128, 128, 128)
    }
    color = color_map.get(state, (255, 255, 255))
    cv2.rectangle(frame, (0, 0), (640, 50), color, -1)
    status_text = f"STATUS: {state.name} ({decision.confidence:.2f})"
    if state == FocusState.FOCUSED: status_text += " - KEEP GOING"
    else: status_text += f" - {decision.reason}"
    cv2.putText(frame, status_text, (10, 35), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0,0,0), 2)
    cv2.putText(frame, f"SCORE: {snap.score:.1f}", (10, 80), cv2.FONT_HERSHEY_SIMPLEX, 1.0, color, 2)
    
    debug = feats["debug"]
    if debug.get("face_center"): cv2.circle(frame, debug["face_center"], 5, (0, 255, 0), -1)
    if debug.get("phone_box"):
        x1, y1, x2, y2 = debug["phone_box"]
        cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 255), 2)
    for hx, hy in debug["hand_centers"]: cv2.circle(frame, (hx, hy), 5, (255, 0, 255), -1)



# ==========================================
# Main Control
# ==========================================
def main():
    parser = argparse.ArgumentParser(description='Focus Monitor AI')
    parser.add_argument('--session-id', type=str, required=True, help='Session ID from Backend')
    args = parser.parse_args()
    
    current_session_id = args.session_id
    print(f"[INFO] Starting Monitor for Session: {current_session_id}")
    extractor = FeatureExtractor()
    abs_det, drowsy_det, phone_det = AbsenceDetector(), DrowsinessDetector(), PhoneDetector()
    fuser, scorer, logger = StateFuser(), FocusScorer(), CSVLogger()
    
    # Kafka Logger 초기화
    kafka_logger = KafkaLogger(session_id=current_session_id)
    
    print("Initializing Camera...")
    cap = cv2.VideoCapture(0, cv2.CAP_DSHOW)
    if not cap.isOpened():
        cap = cv2.VideoCapture(1, cv2.CAP_DSHOW)
        if not cap.isOpened(): print("[ERROR] Camera failed."); return

    print("Focus Monitor Started. Press 'q' to quit.")

    try: # [추가] 예외 처리를 시작합니다.
        while cap.isOpened():
            ret, frame = cap.read()
            if not ret: break
            frame = cv2.flip(frame, 1) # [수정] 거울 모드 적용
            
            feats = extractor.process(frame)
<<<<<<< HEAD
            sig_drowsy = drowsy_det.process(feats["face_detected"], feats["ear"], feats["pitch"])
            sig_phone = phone_det.process(feats["phone_conf"], feats["face_detected"], feats["pitch"], feats["hand_interaction"])
            sig_abs = abs_det.process(feats["face_present"], sig_phone.phone_present)
=======
            sig_abs = abs_det.process(feats["face_detected"])
            sig_drowsy = drowsy_det.process(feats["face_detected"], feats["ear"], feats["pitch"])
            sig_phone = phone_det.process(feats["phone_conf"], feats["face_detected"], feats["pitch"], feats["hand_interaction"])
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
            
            signals = FrameSignals(drowsy=sig_drowsy, absent=sig_abs, phone=sig_phone)
            decision = fuser.decide(signals)
            snap = scorer.update(decision.state)
            
            # [수정] 엑셀 이미지(image_e988b2.png)에 나오는 모든 컬럼을 기록합니다.
            logger.log({
                'state': decision.state.name,
                'score': round(snap.score, 2),
                'ear': round(feats['ear'], 3) if feats['ear'] else None,
                'pitch': round(feats['pitch'], 3) if feats['pitch'] else None,
                'phone_conf': round(feats['phone_conf'], 3),
                'hand_interaction': 1 if feats['hand_interaction'] else 0,
                'raw_absent': 1 if signals.absent.absent else 0,
                'drowsy_score': round(signals.drowsy.drowsy_score, 2),
                'phone_in_use': 1 if signals.phone.phone_in_use else 0
            })
            
            # Kafka로 상태 전송
            kafka_logger.log_state(decision.state)

            draw_ui(frame, decision, snap, feats, signals)
            cv2.imshow("Focus Monitor", frame)
            if cv2.waitKey(1) & 0xFF == ord('q'): break
            
    finally: # [핵심] 프로그램이 중간에 멈추거나 꺼져도 반드시 실행됩니다.
        print("[INFO] Saving all buffered data to CSV...")
        cap.release()
        cv2.destroyAllWindows()
        logger.close() # 이 코드가 실행되어야 파일이 비어있지 않게 됩니다.
        kafka_logger.close()

if __name__ == "__main__":
    try:
        main()
    except Exception as e:
<<<<<<< HEAD
        print(f"[FATAL ERROR] {e}")
=======
        print(f"[FATAL ERROR] {e}")
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
