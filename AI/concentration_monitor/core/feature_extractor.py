import cv2
import numpy as np
import mediapipe as mp
import mediapipe.python.solutions
import time
from ultralytics import YOLO
from core.config import Config

class FeatureExtractor:
    def __init__(self):
        self.mp_face = mp.solutions.face_mesh
        self.face = self.mp_face.FaceMesh(
            max_num_faces=1, refine_landmarks=True,
            min_detection_confidence=0.85, min_tracking_confidence=0.85
        )
<<<<<<< HEAD
        self.mp_face_detection = mp.solutions.face_detection
        self.face_detection = self.mp_face_detection.FaceDetection(
            min_detection_confidence=0.5
        )
=======
        self.face_frame_index = 0
        self.last_face_detected = False
        self.last_ear = None
        self.last_pitch = None
        self.last_face_pixel_center = None
        self.last_face_updated = False
        self.last_face_bbox = None
>>>>>>> 1d12e087b06e8ccc4de00953fd963920a5f14c00
        self.mp_hands = mp.solutions.hands
        self.hands = None
        if Config.ENABLE_HANDS:
            self.hands = self.mp_hands.Hands(
                max_num_hands=2, min_detection_confidence=0.5, min_tracking_confidence=0.5
            )
        self.hands_frame_index = 0
        self.last_hand_centers = []
        self.hand_cache_age = 0
        try:
<<<<<<< HEAD
            self.yolo = YOLO("bestv7.pt")
=======
            # Use Nano for faster inference
            self.yolo = YOLO("yolov8n.pt")
>>>>>>> 1d12e087b06e8ccc4de00953fd963920a5f14c00
            self.yolo_names = self.yolo.names
            print(f"내 모델 클래스 목록: {self.yolo.names}")
            # 출력 예시: {0: 'phone'} -> 이러면 0번이 맞음!
        except Exception as e:
            print(f"[WARN] YOLO Load Failed: {e}")
            self.yolo = None
        self.frame_index = 0
        self.last_phone_conf = 0.0
        self.last_phone_box = None
        self.last_is_cell_phone = False
        self.last_detected_classes = []
        self.phone_cache_age = 0
        self.last_yolo_time = 0.0
        self.last_phone_detect_time = None

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

<<<<<<< HEAD
    def process(self, frame, detect_hands: bool = True, detect_phone: bool = True):
        h, w, _ = frame.shape
        rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        face_res = self.face.process(rgb)
        face_detected, ear, pitch, face_pixel_center = False, None, None, None
        face_present = False
        ghost_filtered = False
        
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
                ghost_filtered = True
                # We interpret this as "No Face" (Absent) rather than "Sleep"
            
        hand_centers = []
        if detect_hands:
            hand_res = self.hands.process(rgb)
            if hand_res.multi_hand_landmarks:
                for hand in hand_res.multi_hand_landmarks:
                    sx, sy = sum([l.x for l in hand.landmark]) / 21, sum([l.y for l in hand.landmark]) / 21
                    hand_centers.append((int(sx*w), int(sy*h)))

        phone_conf, phone_box = 0.0, None
        if detect_phone and self.yolo:
            # [Fix] Revert to Phone Only (Round 6)
            results = self.yolo(frame, verbose=False, classes=[0], conf=0.4)
=======
    def process(self, frame):
        self.frame_index += 1
        h, w, _ = frame.shape
        rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        face_detected = self.last_face_detected
        ear = self.last_ear
        pitch = self.last_pitch
        face_pixel_center = self.last_face_pixel_center
        face_updated = False

        self.face_frame_index += 1
        run_face = (self.face_frame_index % Config.FACE_EVERY_N_FRAMES == 0)
        if run_face:
            face_res = self.face.process(rgb)
            face_detected, ear, pitch, face_pixel_center = False, None, None, None
            
            if face_res.multi_face_landmarks:
                face_detected = True
                lm = face_res.multi_face_landmarks[0].landmark
                ear, pitch = self._calc_ear(lm, w, h), self._calc_head_pitch(lm)
                face_pixel_center = (int((lm[234].x + lm[454].x)/2 * w), int((lm[10].y + lm[152].y)/2 * h))
                min_x = min(p.x for p in lm) * w
                min_y = min(p.y for p in lm) * h
                max_x = max(p.x for p in lm) * w
                max_y = max(p.y for p in lm) * h
                self.last_face_bbox = (min_x, min_y, max_x, max_y)
                
                # [Fix] Removed EAR < 0.02 ghost check as it causes AWAY during deep sleep
                pass
            else:
                self.last_face_bbox = None
            self.last_face_detected = face_detected
            self.last_ear = ear
            self.last_pitch = pitch
            self.last_face_pixel_center = face_pixel_center
            face_updated = True
        self.last_face_updated = face_updated
            
        hand_centers = list(self.last_hand_centers)
        if self.hands:
            self.hands_frame_index += 1
            now = time.time()
            force_hands = self.last_phone_detect_time is not None and (now - self.last_phone_detect_time) <= Config.HANDS_BURST_SEC
            if Config.HANDS_ONLY_ON_PHONE:
                run_hands = force_hands and (self.hands_frame_index % Config.HAND_EVERY_N_FRAMES == 0)
            else:
                run_hands = force_hands or (self.hands_frame_index % Config.HAND_EVERY_N_FRAMES == 0)
            if run_hands:
                hand_res = self.hands.process(rgb)
                hand_centers = []
                if hand_res.multi_hand_landmarks:
                    for hand in hand_res.multi_hand_landmarks:
                        sx, sy = sum([l.x for l in hand.landmark]) / 21, sum([l.y for l in hand.landmark]) / 21
                        hand_centers.append((int(sx*w), int(sy*h)))
                self.last_hand_centers = hand_centers
                self.hand_cache_age = 0
            else:
                if not force_hands:
                    self.last_hand_centers = []
                    hand_centers = []
                    self.hand_cache_age = 0
                else:
                    self.hand_cache_age += 1
                    if self.hand_cache_age > Config.HAND_CACHE_MAX_AGE:
                        self.last_hand_centers = []
                        hand_centers = []
        else:
            self.last_hand_centers = []
            hand_centers = []

        phone_conf = self.last_phone_conf
        phone_box = self.last_phone_box
        is_cell_phone = self.last_is_cell_phone
        detected_classes = list(self.last_detected_classes)
        prev_phone_conf = self.last_phone_conf
        prev_phone_box = self.last_phone_box
        prev_is_cell_phone = self.last_is_cell_phone
        prev_phone_time = self.last_phone_detect_time
        now = time.time()
        use_interval = Config.YOLO_INTERVAL_SEC is not None and Config.YOLO_INTERVAL_SEC > 0
        run_yolo = False
        if self.yolo:
            if use_interval:
                run_yolo = (now - self.last_yolo_time) >= Config.YOLO_INTERVAL_SEC
            else:
                run_yolo = (self.frame_index % Config.YOLO_EVERY_N_FRAMES == 0)
        if run_yolo:
            self.last_yolo_time = now
            # On YOLO frames, reset and use only current detections (no stale boxes).
            phone_conf = 0.0
            phone_box = None
            is_cell_phone = False
            detected_classes = []
            detected = False
            distractor_conf = 0.0
            distractor_box = None
            yolo_frame = frame
            scale_x = scale_y = 1.0
            if Config.YOLO_IMG_SIZE and (w > Config.YOLO_IMG_SIZE or h > Config.YOLO_IMG_SIZE):
                scale = Config.YOLO_IMG_SIZE / max(w, h)
                new_w, new_h = int(w * scale), int(h * scale)
                yolo_frame = cv2.resize(frame, (new_w, new_h), interpolation=cv2.INTER_AREA)
                scale_x = w / new_w
                scale_y = h / new_h
            yolo_classes = [67] + list(Config.PHONE_DISTRACTOR_CLASSES)
            results = self.yolo(yolo_frame, verbose=False, classes=yolo_classes, conf=0.03)
>>>>>>> 1d12e087b06e8ccc4de00953fd963920a5f14c00
            for r in results:
                for box in r.boxes:
                    cls_id = int(box.cls[0])
                    conf = float(box.conf[0])
<<<<<<< HEAD
                    # [DEBUG_INTERNAL] Print what YOLO sees locally
                    # print(f"[DEBUG_INTERNAL] YOLO saw class {cls_id} with conf {conf:.3f}")
                    
                    if cls_id == 0: # Cell phone
=======
                    cls_name = self.yolo_names.get(cls_id, 'unknown')
                    detected_classes.append(cls_name)
                    if cls_id in Config.PHONE_DISTRACTOR_CLASSES:
                        if conf > distractor_conf:
                            d_xyxy = box.xyxy[0].tolist()
                            if scale_x != 1.0 or scale_y != 1.0:
                                d_xyxy = [d_xyxy[0] * scale_x, d_xyxy[1] * scale_y, d_xyxy[2] * scale_x, d_xyxy[3] * scale_y]
                            distractor_conf = conf
                            distractor_box = list(map(int, d_xyxy))
                    if cls_id == 67: # Cell phone only
>>>>>>> 1d12e087b06e8ccc4de00953fd963920a5f14c00
                        if conf > phone_conf:
                            phone_conf = conf
                            xyxy = box.xyxy[0].tolist()
                            if scale_x != 1.0 or scale_y != 1.0:
                                xyxy = [xyxy[0] * scale_x, xyxy[1] * scale_y, xyxy[2] * scale_x, xyxy[3] * scale_y]
                            phone_box = list(map(int, xyxy))
                            is_cell_phone = True
            distractor_overlap = False
            if phone_box is not None and distractor_box is not None:
                ix1 = max(phone_box[0], distractor_box[0])
                iy1 = max(phone_box[1], distractor_box[1])
                ix2 = min(phone_box[2], distractor_box[2])
                iy2 = min(phone_box[3], distractor_box[3])
                iw = max(0, ix2 - ix1)
                ih = max(0, iy2 - iy1)
                inter = iw * ih
                if inter > 0:
                    p_area = max(1, (phone_box[2] - phone_box[0]) * (phone_box[3] - phone_box[1]))
                    d_area = max(1, (distractor_box[2] - distractor_box[0]) * (distractor_box[3] - distractor_box[1]))
                    iou = inter / float(p_area + d_area - inter)
                    if iou >= Config.PHONE_DISTRACTOR_IOU_TH:
                        distractor_overlap = True

            distractor_hit = (
                distractor_overlap
                and (distractor_conf >= Config.PHONE_DISTRACTOR_CONF_TH)
                and (phone_conf < (distractor_conf + Config.PHONE_DISTRACTOR_MARGIN))
            )
            if distractor_hit:
                phone_conf = 0.0
                phone_box = None
                is_cell_phone = False
            if phone_box is not None and phone_conf < Config.PHONE_CANDIDATE_TH:
                phone_box = None
                is_cell_phone = False
            if phone_box is not None and phone_conf >= Config.PHONE_CANDIDATE_TH:
                detected = True
                self.last_phone_detect_time = now
            if (not detected) and (not distractor_hit) and prev_phone_time is not None and (now - prev_phone_time) <= Config.PHONE_CACHE_TTL_SEC:
                phone_conf = prev_phone_conf
                phone_box = prev_phone_box
                is_cell_phone = prev_is_cell_phone
            self.last_phone_conf = phone_conf
            self.last_phone_box = phone_box
            self.last_is_cell_phone = is_cell_phone
            self.last_detected_classes = detected_classes
            self.phone_cache_age = 0
        else:
            # Clear cached phone box if it is too old in time.
            if self.last_phone_detect_time is not None and (now - self.last_phone_detect_time) > Config.PHONE_CACHE_TTL_SEC:
                self.last_phone_conf = 0.0
                self.last_phone_box = None
                self.last_is_cell_phone = False
                self.last_detected_classes = []
            if use_interval:
                # When using time-based YOLO, rely on TTL only to avoid rapid drops.
                self.phone_cache_age = 0
            else:
                self.phone_cache_age += 1
                if self.phone_cache_age > Config.PHONE_CACHE_MAX_AGE:
                    self.last_phone_conf = 0.0
                    self.last_phone_box = None
                    self.last_is_cell_phone = False
                    self.last_detected_classes = []
                    phone_conf = 0.0
                    phone_box = None
                    is_cell_phone = False
                    detected_classes = []
        
        # [Fix] Smart Hybrid Liveness Filter (Round 7)
        # Combines Motion (Speed) + Blink (Safety Switch)
        if face_detected and face_pixel_center:
            is_blinking = (ear is not None and ear < 0.20)
            
            if self.prev_face_center:
                dist = ((face_pixel_center[0] - self.prev_face_center[0])**2 + 
                        (face_pixel_center[1] - self.prev_face_center[1])**2)**0.5
                
                # Condition to Reset: If user blinks OR moves > 1.0px (More sensitive)
                if is_blinking or dist > 1.0:
                    self.static_frames = 0
                else:
                    self.static_frames += 1
            
            if self.static_frames > 100:
<<<<<<< HEAD
                face_detected = False # Treat as Ghost (Inanimate object)
                ear, pitch = None, None
                ghost_filtered = True
=======
                # [Fix] Enhanced Liveness: If eyes are even slightly closed or pitch is high,
                # it's definitely a person. Never treat as ghost in these states.
                if is_blinking or (pitch is not None and pitch > 0.3):
                    self.static_frames = 0
                else:
                    face_detected = False 
                    ear, pitch = None, None
            self.prev_face_center = face_pixel_center if face_detected else None
>>>>>>> 1d12e087b06e8ccc4de00953fd963920a5f14c00
        else:
            self.static_frames = 0
            self.prev_face_center = None

<<<<<<< HEAD
        if face_detected:
            face_present = True
        elif not ghost_filtered:
            det_res = self.face_detection.process(rgb)
            if det_res.detections:
                face_present = True
=======
        hand_near_face = False
        if face_pixel_center and hand_centers:
            fx, fy = face_pixel_center
            # Thresh: 25% of image diagonal
            thresh = (w**2 + h**2)**0.5 * 0.25
            for hx, hy in hand_centers:
                if ((fx-hx)**2 + (fy-hy)**2)**0.5 < thresh:
                    hand_near_face = True; break
>>>>>>> 1d12e087b06e8ccc4de00953fd963920a5f14c00

        hand_interaction = False
        if detect_phone and phone_box and hand_centers:
            px, py = (phone_box[0] + phone_box[2]) // 2, (phone_box[1] + phone_box[3]) // 2
            thresh = (w**2 + h**2)**0.5 * 0.2
            for hx, hy in hand_centers:
                if ((px-hx)**2 + (py-hy)**2)**0.5 < thresh:
                    hand_interaction = True; break

        phone_near_face = False
        phone_area_ratio = 0.0
        if phone_box and face_pixel_center:
            fx, fy = face_pixel_center
            px, py = (phone_box[0] + phone_box[2]) // 2, (phone_box[1] + phone_box[3]) // 2
            thresh = (w**2 + h**2)**0.5 * Config.PHONE_NEAR_FACE_RATIO
            if self.last_face_bbox:
                face_bottom = self.last_face_bbox[3]
                below_face = py > (face_bottom + (Config.PHONE_NEAR_FACE_Y_OFFSET * h))
            else:
                below_face = py > (fy + (Config.PHONE_NEAR_FACE_Y_OFFSET * h))
            if below_face and ((px-fx)**2 + (py-fy)**2)**0.5 < thresh:
                phone_near_face = True
        if phone_box:
            pw = max(0, phone_box[2] - phone_box[0])
            ph = max(0, phone_box[3] - phone_box[1])
            phone_area_ratio = (pw * ph) / float(w * h) if w > 0 and h > 0 else 0.0

        return {
<<<<<<< HEAD
            "face_detected": face_detected, "face_present": face_present,
            "ear": ear, "pitch": pitch,
            "phone_conf": phone_conf, "hand_interaction": hand_interaction,
            "debug": {"face_center": face_pixel_center, "phone_box": phone_box, "hand_centers": hand_centers}
=======
            "face_detected": face_detected, "face_updated": face_updated, "ear": ear, "pitch": pitch,
            "phone_conf": phone_conf, 
            "is_cell_phone": is_cell_phone,
            "hand_interaction": hand_interaction,
            "hand_near_face": hand_near_face,
            "phone_near_face": phone_near_face,
            "phone_area_ratio": phone_area_ratio,
            "debug": {"face_center": face_pixel_center, "phone_box": phone_box, "hand_centers": hand_centers, "detected_classes": detected_classes}
>>>>>>> 1d12e087b06e8ccc4de00953fd963920a5f14c00
        }
