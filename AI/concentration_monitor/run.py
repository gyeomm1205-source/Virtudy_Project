# run.py
import sys
import time
print("[INFO] Loading System Libraries...")
import cv2
import numpy as np

print("[INFO] Loading AI Models (MediaPipe)...")
import mediapipe as mp

print("[INFO] Loading AI Models (YOLO)...")
from ultralytics import YOLO

# Logic Modules
print("[INFO] Loading Logic Modules...", flush=True)
try:
    from core.types import FrameSignals, FocusDecision, FocusState
    from core.config import Config
    from detectors.absence_detector import AbsenceDetector
    from detectors.drowsiness_detector import DrowsinessDetector
    from detectors.phone_detector import PhoneDetector
    from fusion.state_fuser import StateFuser
    from scoring.focus_scorer import FocusScorer
    from utils.csv_logger import CSVLogger
    print("[INFO] Logic Modules Loaded.", flush=True)
except Exception as e:
    print(f"[ERROR] Failed to load logic modules: {e}", flush=True)
    sys.exit(1)

# ==========================================
# Feature Extraction Helpers
# ==========================================
class FeatureExtractor:
    def __init__(self):
        # MediaPipe
        self.mp_face = mp.solutions.face_mesh
        self.face = self.mp_face.FaceMesh(
            max_num_faces=1,
            refine_landmarks=True,
            min_detection_confidence=0.5,
            min_tracking_confidence=0.5
        )
        self.mp_hands = mp.solutions.hands
        self.hands = self.mp_hands.Hands(
            max_num_hands=2,
            min_detection_confidence=0.5,
            min_tracking_confidence=0.5
        )
        # YOLO
        try:
            self.yolo = YOLO("yolov8n.pt")
            self.yolo_names = self.yolo.names
        except Exception as e:
            print(f"[WARN] YOLO Load Failed: {e}")
            self.yolo = None

        # Constants for EAR
        self.LEFT_EYE = [33, 160, 158, 133, 153, 144]
        self.RIGHT_EYE = [362, 385, 387, 263, 373, 380]

    def _calc_ear(self, landmarks, w, h):
        def dist(p1, p2):
            return np.linalg.norm(np.array([landmarks[p1].x*w, landmarks[p1].y*h]) - 
                                  np.array([landmarks[p2].x*w, landmarks[p2].y*h]))
        
        # Left Error
        l_v1 = dist(160, 144)
        l_v2 = dist(158, 153)
        l_h = dist(33, 133)
        ear_l = (l_v1 + l_v2) / (2.0 * l_h) if l_h > 1e-6 else 0
        
        # Right Ear
        r_v1 = dist(385, 380)
        r_v2 = dist(387, 373)
        r_h = dist(362, 263)
        ear_r = (r_v1 + r_v2) / (2.0 * r_h) if r_h > 1e-6 else 0
        
        return (ear_l + ear_r) / 2.0

    def _calc_head_pitch(self, landmarks):
        # Simple ratio based pitch (from phone_detector.py)
        # ratio = (nose_y - eye_mid_y) / (chin_y - eye_mid_y)
        # Higher Ratio (~0.6+) means looking down
        nose_y = landmarks[1].y
        chin_y = landmarks[152].y
        eye_mid_y = (landmarks[33].y + landmarks[263].y) / 2.0
        
        denom = (chin_y - eye_mid_y)
        if denom < 1e-6:
            return 0.0
        
        ratio = (nose_y - eye_mid_y) / denom
        return ratio

    def process(self, frame):
        h, w, _ = frame.shape
        rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        
        # 1. Face
        face_res = self.face.process(rgb)
        face_detected = False
        ear = None
        pitch = None
        face_pixel_center = None
        
        if face_res.multi_face_landmarks:
            face_detected = True
            # Pick largest face? Just first one for now.
            lm = face_res.multi_face_landmarks[0].landmark
            
            ear = self._calc_ear(lm, w, h)
            pitch = self._calc_head_pitch(lm) # Returning Ratio
            
            # Center for UI
            cx = int((lm[234].x + lm[454].x)/2 * w)
            cy = int((lm[10].y + lm[152].y)/2 * h)
            face_pixel_center = (cx, cy)
            
        # 2. Hands
        hand_res = self.hands.process(rgb)
        hand_centers = []
        if hand_res.multi_hand_landmarks:
            for hand in hand_res.multi_hand_landmarks:
                # Calc center
                sx = sum([l.x for l in hand.landmark]) / 21
                sy = sum([l.y for l in hand.landmark]) / 21
                hand_centers.append((int(sx*w), int(sy*h)))

        # 3. YOLO (Phone)
        phone_conf = 0.0
        phone_box = None
        if self.yolo:
            results = self.yolo(frame, verbose=False, classes=[67]) # 67 is cell phone in COCO?
            # Check classes? 'cell phone' is usually index 67 in COCO80, 
            # but yolo.names values might differ?
            # Safer to check name.
            
            best_conf = 0.0
            for r in results:
                for box in r.boxes:
                    cls_id = int(box.cls[0])
                    name = self.yolo_names[cls_id]
                    if name == 'cell phone':
                        conf = float(box.conf[0])
                        if conf > best_conf:
                            best_conf = conf
                            x1, y1, x2, y2 = map(int, box.xyxy[0])
                            phone_box = (x1, y1, x2, y2)
            phone_conf = best_conf

        # 4. Hand Interaction
        # Dist between phone center and hand centers
        hand_interaction = False
        if phone_box and hand_centers:
            px = (phone_box[0] + phone_box[2]) // 2
            py = (phone_box[1] + phone_box[3]) // 2
            
            diag = (w**2 + h**2)**0.5
            thresh = diag * 0.3 # Increased from 0.2 to 0.3 to catch holding phone loosely
            
            for hx, hy in hand_centers:
                dist = ((px-hx)**2 + (py-hy)**2)**0.5
                if dist < thresh:
                    hand_interaction = True
                    break

        return {
            "face_detected": face_detected,
            "ear": ear,
            "pitch": pitch,
            "phone_conf": phone_conf,
            "hand_interaction": hand_interaction,
            "debug": {
                "face_center": face_pixel_center,
                "phone_box": phone_box,
                "hand_centers": hand_centers
            }
        }


def main():
    # 1. Update Config for Ratio-based Pitch (Tuned based on logs)
    # User's Normal Pitch (Focused) is approx 0.30 ~ 0.33.
    # We need thresholds slightly above this.
    Config.PITCH_DROWSY_TH = 0.45   # Was 0.65 (Too high)
    Config.PITCH_PHONE_USE_TH = 0.40 # Was 0.60 (Too high)
    
    # Also lower EAR threshold slightly just in case
    Config.EAR_DROWSY_TH = 0.18

    # 2. Init Modules
    extractor = FeatureExtractor()
    abs_det = AbsenceDetector()
    drowsy_det = DrowsinessDetector()
    phone_det = PhoneDetector()
    fuser = StateFuser()
    scorer = FocusScorer()
    logger = CSVLogger()
    
    print("Initializing Camera...")
    # Try DirectShow (backend constant 700) for Windows compatibility
    cap = cv2.VideoCapture(0, cv2.CAP_DSHOW)
    
    if not cap.isOpened():
        print("[WARN] Camera index 0 could not be opened. Trying index 1...")
        cap = cv2.VideoCapture(1, cv2.CAP_DSHOW)
        if not cap.isOpened():
            print("[ERROR] Could not open any camera (0 or 1). Please check connection.")
            return

    print("Focus Monitor Started. Press 'q' to quit.")

    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            break
        
        # A. Feature Extraction
        feats = extractor.process(frame)
        
        # B. Detection
        sig_abs = abs_det.process(feats["face_detected"])
        sig_drowsy = drowsy_det.process(feats["face_detected"], feats["ear"], feats["pitch"])
        sig_phone = phone_det.process(feats["phone_conf"], feats["face_detected"], feats["pitch"], feats["hand_interaction"])
        
        # C. Fusion
        signals = FrameSignals(drowsy=sig_drowsy, absent=sig_abs, phone=sig_phone)
        decision = fuser.decide(signals)
        
        # D. Scoring
        snap = scorer.update(decision.state)
        
        # E. Logging
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

        # F. UI Drawing
        draw_ui(frame, decision, snap, feats, signals)
        
        cv2.imshow("Focus Monitor", frame)
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break
            
    cap.release()
    cv2.destroyAllWindows()
    logger.close()

def draw_ui(frame, decision, snap, feats, signals):
    state = decision.state
    # Colors
    color_map = {
        FocusState.FOCUSED: (0, 255, 0),
        FocusState.DROWSY: (0, 0, 255),
        FocusState.PHONE: (0, 255, 255),
        FocusState.ABSENT: (255, 0, 0),
        FocusState.UNKNOWN: (128, 128, 128)
    }
    color = color_map.get(state, (255, 255, 255))
    
    # 1. Big Status Bar
    cv2.rectangle(frame, (0, 0), (640, 50), color, -1)
    
    status_text = f"STATUS: {state.name} ({decision.confidence:.2f})"
    if state == FocusState.FOCUSED:
        status_text += " - KEEP GOING"
    else:
        status_text += f" - {decision.reason}"
        
    cv2.putText(frame, status_text, (10, 35), 
                cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0,0,0), 2)

    # 2. Score
    cv2.putText(frame, f"SCORE: {snap.score:.1f}", (10, 80),
                cv2.FONT_HERSHEY_SIMPLEX, 1.0, color, 2)
    
    # 3. Debug Info (Signal Values)
    y = 120
    lines = [
        f"EAR: {feats['ear']:.2f}" if feats['ear'] else "EAR: N/A",
        f"Pitch: {feats['pitch']:.2f}" if feats['pitch'] else "Pitch: N/A",
        f"PhoneConf: {feats['phone_conf']:.2f}",
        f"HandInteract: {feats['hand_interaction']}",
        f"RawStates -> Abs:{signals.absent.absent} Drw:{signals.drowsy.drowsy_score:.1f} Ph:{signals.phone.phone_in_use}"
    ]
    for line in lines:
        cv2.putText(frame, line, (10, y), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (200, 200, 200), 1)
        y += 20

    # 4. Bounding Boxes
    debug = feats["debug"]
    
    # Face Center
    if debug["face_center"]:
        cv2.circle(frame, debug["face_center"], 5, (0, 255, 0), -1)
    
    # Phone Box
    if debug["phone_box"]:
        x1, y1, x2, y2 = debug["phone_box"]
        cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 255), 2)
        cv2.putText(frame, "Phone", (x1, y1-5), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 255), 1)
        
    # Hands
    for hx, hy in debug["hand_centers"]:
        cv2.circle(frame, (hx, hy), 5, (255, 0, 255), -1)

if __name__ == "__main__":
    print("[INFO] Script Main Entry Point Reached.", flush=True)
    try:
        main()
    except Exception as e:
        print(f"[FATAL ERROR] Main Crashed: {e}", flush=True)
        import traceback
        traceback.print_exc()
