import cv2
import numpy as np
import mediapipe as mp
import mediapipe.python.solutions
from ultralytics import YOLO

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
                    # print(f"[DEBUG_INTERNAL] YOLO saw class {cls_id} with conf {conf:.3f}")
                    
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
