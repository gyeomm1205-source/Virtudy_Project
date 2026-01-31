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
class FeatureExtractor:
    def __init__(self):
        self.mp_face = mp.solutions.face_mesh
        self.face = self.mp_face.FaceMesh(
            max_num_faces=1, refine_landmarks=True,
            min_detection_confidence=0.85, min_tracking_confidence=0.85
        )
        self.mp_hands = mp.solutions.hands
        self.hands = self.mp_hands.Hands(
            max_num_hands=2, min_detection_confidence=0.5, min_tracking_confidence=0.5
        )
        try:
            self.yolo = YOLO("yolov8n.pt")
            self.yolo_names = self.yolo.names
        except Exception as e:
            print(f"[WARN] YOLO Load Failed: {e}")
            self.yolo = None
            
        # Round 6: Static Filter Variables
        self.prev_face_center = None
        self.static_frames = 0

    def _calc_ear(self, landmarks, w, h):
        def dist(p1, p2):
            return np.linalg.norm(np.array([landmarks[p1].x*w, landmarks[p1].y*h]) - 
                                  np.array([landmarks[p2].x*w, landmarks[p2].y*h]))
        l_v1, l_v2, l_h = dist(160, 144), dist(158, 153), dist(33, 133)
        ear_l = (l_v1 + l_v2) / (2.0 * l_h) if l_h > 1e-6 else 0
        r_v1, r_v2, r_h = dist(385, 380), dist(387, 373), dist(362, 263)
        ear_r = (r_v1 + r_v2) / (2.0 * r_h) if r_h > 1e-6 else 0
        return (ear_l + ear_r) / 2.0

    def _calc_head_pitch(self, landmarks):
        nose_y, chin_y = landmarks[1].y, landmarks[152].y
        eye_mid_y = (landmarks[33].y + landmarks[263].y) / 2.0
        denom = (chin_y - eye_mid_y)
        return (nose_y - eye_mid_y) / denom if denom > 1e-6 else 0.0

    def process(self, frame):
        h, w, _ = frame.shape
        rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        face_res = self.face.process(rgb)
        face_detected, ear, pitch, face_pixel_center = False, None, None, None
        
        if face_res.multi_face_landmarks:
            face_detected = True
            lm = face_res.multi_face_landmarks[0].landmark
            ear, pitch = self._calc_ear(lm, w, h), self._calc_head_pitch(lm)
            face_pixel_center = (int((lm[234].x + lm[454].x)/2 * w), int((lm[10].y + lm[152].y)/2 * h))
            
            # [Fix] Ghost Face Filter
            # If EAR is suspiciously low (< 0.02), it's likely a non-human object (chair, wall pattern)
            # incorrectly identified as a face. Real closed eyes are usually ~0.15-0.18.
            # (User feedback: 0.05 was too high and filtered real sleep)
            if ear < 0.02:
                face_detected = False
                # We interpret this as "No Face" (Absent) rather than "Sleep"
            
        hand_res = self.hands.process(rgb)
        hand_centers = []
        if hand_res.multi_hand_landmarks:
            for hand in hand_res.multi_hand_landmarks:
                sx, sy = sum([l.x for l in hand.landmark]) / 21, sum([l.y for l in hand.landmark]) / 21
                hand_centers.append((int(sx*w), int(sy*h)))

        phone_conf, phone_box = 0.0, None
        if self.yolo:
            # [Fix] Revert to Phone Only (Round 6)
            results = self.yolo(frame, verbose=False, classes=[67], conf=0.05)
            for r in results:
                for box in r.boxes:
                    cls_id = int(box.cls[0])
                    conf = float(box.conf[0])
                    # [DEBUG_INTERNAL] Print what YOLO sees locally
                    print(f"[DEBUG_INTERNAL] YOLO saw class {cls_id} with conf {conf:.3f}")
                    
                    if cls_id == 67: # Cell phone
                        if conf > phone_conf:
                            phone_conf = conf
                            phone_box = list(map(int, box.xyxy[0]))
        
        # [Fix] Smart Hybrid Liveness Filter (Round 7)
        # Combines Motion (Speed) + Blink (Safety Switch)
        if face_detected and face_pixel_center:
            is_blinking = (ear is not None and ear < 0.20)
            
            if self.prev_face_center:
                dist = ((face_pixel_center[0] - self.prev_face_center[0])**2 + 
                        (face_pixel_center[1] - self.prev_face_center[1])**2)**0.5
                
                # Condition to Reset: If user blinks OR moves > 1.5px
                if is_blinking or dist > 1.5:
                    self.static_frames = 0
                else:
                    self.static_frames += 1
            
            self.prev_face_center = face_pixel_center
            
            # Threshold: 5 seconds of ABSOLUTE stillness and NO blinks
            # (Assuming ~20 FPS -> 100 frames)
            if self.static_frames > 100:
                face_detected = False # Treat as Ghost (Inanimate object)
                ear, pitch = None, None
        else:
            self.static_frames = 0
            self.prev_face_center = None

        hand_interaction = False
        if phone_box and hand_centers:
            px, py = (phone_box[0] + phone_box[2]) // 2, (phone_box[1] + phone_box[3]) // 2
            thresh = (w**2 + h**2)**0.5 * 0.3
            for hx, hy in hand_centers:
                if ((px-hx)**2 + (py-hy)**2)**0.5 < thresh:
                    hand_interaction = True; break

        return {
            "face_detected": face_detected, "ear": ear, "pitch": pitch,
            "phone_conf": phone_conf, "hand_interaction": hand_interaction,
            "debug": {"face_center": face_pixel_center, "phone_box": phone_box, "hand_centers": hand_centers}
        }

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
    # 원래 최적화했던 수치들 복구
    Config.PITCH_DROWSY_TH, Config.PITCH_PHONE_USE_TH = 0.45, 0.40
    Config.EAR_DROWSY_TH = 0.18

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
            sig_abs = abs_det.process(feats["face_detected"])
            sig_drowsy = drowsy_det.process(feats["face_detected"], feats["ear"], feats["pitch"])
            sig_phone = phone_det.process(feats["phone_conf"], feats["face_detected"], feats["pitch"], feats["hand_interaction"])
            
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
        print(f"[FATAL ERROR] {e}")