# phone_detector.py
import time
import cv2
import mediapipe as mp
from ultralytics import YOLO


class PhoneDetector:
    """
    개선된 폰 사용 감지:
    - YOLO: cell phone 탐지
    - MediaPipe Hands: 손 중심 추정
    - MediaPipe FaceMesh: 고개 숙임(pitch) 보조 신호 (2D ratio + EMA)
    - ON 조건:
        (A) phone_present AND (hand_near OR head_down) 가 fast_on_hold_sec 이상 유지 -> ON (빠르게)
        (B) phone_present 단독이 phone_only_on_hold_sec 이상 유지 -> ON (fallback)
            단, phone_only는 phone_conf >= conf_th_phone_only 일 때만 허용
    - OFF 조건:
        phone_present가 끊기거나, (hand_near/head_down)이 충분히 사라진 상태가 off_hold_sec 이상 유지 -> OFF
    """

    def __init__(
        self,
        yolo_model="yolov8n.pt",
        conf_th=0.40,
        near_dist_ratio=0.22,

        # 폰만으로 ON되는 최소 conf (phone_only fallback에만 적용)
        conf_th_phone_only=0.60,

        # 빠른 ON(손 or 고개숙임 포함)
        fast_on_hold_sec=0.2,

        # 폰만 있어도 ON 되는 fallback 유지시간
        phone_only_on_hold_sec=0.3,

        # OFF 유지 시간
        off_hold_sec=1.5,

        # 고개 숙임 판정 임계값(튜닝 포인트)
        head_down_ratio_th=0.62,

        # head_ratio 스무딩 강도(클수록 반응 빠름, 작을수록 안정적)
        head_ema_alpha=0.25,

        draw_debug=True,
    ):
        # YOLO
        self.model = YOLO(yolo_model)
        self.names = self.model.names
        self.phone_class = "cell phone"
        self.conf_th = conf_th
        self.conf_th_phone_only = conf_th_phone_only

        # MediaPipe Hands
        self.mp_hands = mp.solutions.hands
        self.hands = self.mp_hands.Hands(
            static_image_mode=False,
            max_num_hands=2,
            model_complexity=0,
            min_detection_confidence=0.5,
            min_tracking_confidence=0.5,
        )

        # MediaPipe FaceMesh
        self.mp_face = mp.solutions.face_mesh
        self.face = self.mp_face.FaceMesh(
            static_image_mode=False,
            max_num_faces=1,
            refine_landmarks=False,
            min_detection_confidence=0.5,
            min_tracking_confidence=0.5,
        )

        # 파라미터
        self.near_dist_ratio = near_dist_ratio
        self.fast_on_hold_sec = fast_on_hold_sec
        self.phone_only_on_hold_sec = phone_only_on_hold_sec
        self.off_hold_sec = off_hold_sec

        self.head_down_ratio_th = head_down_ratio_th
        self.head_ema_alpha = head_ema_alpha
        self.head_ratio_ema = None  # EMA 누적값

        self.draw_debug = draw_debug

        # 상태
        self.state = "OFF"
        self.fast_on_start = None
        self.phone_only_on_start = None
        self.off_start = None

        # 디버그/중간 결과
        self.phone_box = None          # (x1,y1,x2,y2,conf)
        self.hand_centers = []         # [(x,y), ...]
        self.head_down = False
        self.head_ratio = None         # EMA 적용된 ratio (표시용)
        self.last_phone_conf = 0.0     # 표시용

    def close(self):
        self.hands.close()
        self.face.close()

    def _detect_phone(self, frame_bgr):
        """
        반환:
          phone_present(bool), phone_conf(float)
        """
        self.phone_box = None
        self.last_phone_conf = 0.0

        results = self.model(frame_bgr, verbose=False)[0]

        best = None
        best_conf = -1.0

        for box in results.boxes:
            cls_id = int(box.cls[0])
            name = self.names[cls_id]
            conf = float(box.conf[0])

            if name == self.phone_class and conf >= self.conf_th:
                x1, y1, x2, y2 = map(int, box.xyxy[0])
                if conf > best_conf:
                    best_conf = conf
                    best = (x1, y1, x2, y2, conf)

        if best is not None:
            self.phone_box = best
            self.last_phone_conf = float(best[4])
            return True, self.last_phone_conf

        return False, 0.0

    def _detect_hands(self, frame_bgr):
        h, w = frame_bgr.shape[:2]
        self.hand_centers = []

        rgb = cv2.cvtColor(frame_bgr, cv2.COLOR_BGR2RGB)
        res = self.hands.process(rgb)

        if res.multi_hand_landmarks:
            for hand in res.multi_hand_landmarks:
                sx, sy, cnt = 0.0, 0.0, 0
                for lm in hand.landmark:
                    sx += lm.x
                    sy += lm.y
                    cnt += 1
                cx = int((sx / cnt) * w)
                cy = int((sy / cnt) * h)
                self.hand_centers.append((cx, cy))

        return len(self.hand_centers) > 0

    def _detect_head_down(self, frame_bgr):
        """
        FaceMesh로 "고개 숙임"을 2D 비율로 대략 판단합니다.
        ratio = (nose_y - eye_mid_y) / (chin_y - eye_mid_y)
        - EMA 스무딩으로 깜빡임/튐을 줄입니다.
        """
        self.head_down = False
        self.head_ratio = None

        rgb = cv2.cvtColor(frame_bgr, cv2.COLOR_BGR2RGB)
        res = self.face.process(rgb)

        if not res.multi_face_landmarks:
            return False

        lm = res.multi_face_landmarks[0].landmark

        try:
            left_eye = lm[33]
            right_eye = lm[263]
            nose = lm[1]
            chin = lm[152]
        except Exception:
            return False

        eye_mid_y = (left_eye.y + right_eye.y) / 2.0
        denom = (chin.y - eye_mid_y)

        if denom <= 1e-6:
            return False

        ratio = float((nose.y - eye_mid_y) / denom)

        # ✅ EMA 스무딩
        if self.head_ratio_ema is None:
            self.head_ratio_ema = ratio
        else:
            a = self.head_ema_alpha
            self.head_ratio_ema = a * ratio + (1.0 - a) * self.head_ratio_ema

        self.head_ratio = float(self.head_ratio_ema)
        self.head_down = (self.head_ratio_ema >= self.head_down_ratio_th)
        return self.head_down

    def update(self, frame_bgr):
        h, w = frame_bgr.shape[:2]
        diag = (w * w + h * h) ** 0.5
        near_dist = diag * self.near_dist_ratio

        # 1) 폰 (phone_conf 반드시 받기)
        phone_present, phone_conf = self._detect_phone(frame_bgr)

        # 2) 손
        _ = self._detect_hands(frame_bgr)

        # 3) 고개 숙임 (폰이 있을 때만 검사)
        head_down = self._detect_head_down(frame_bgr) if phone_present else False

        # 4) 손-폰 근접
        hand_near = False
        if phone_present and self.hand_centers:
            x1, y1, x2, y2, _conf = self.phone_box
            px = (x1 + x2) // 2
            py = (y1 + y2) // 2

            for hx, hy in self.hand_centers:
                dist = ((hx - px) ** 2 + (hy - py) ** 2) ** 0.5
                if dist <= near_dist:
                    hand_near = True
                    break

        # 5) 상태 전환(시간 누적)
        now = time.time()

        # 빠른 조건: 폰 + (손근접 or 고개숙임)
        fast_condition = phone_present and (hand_near or head_down)

        # 폰만 조건: 폰이 있고, conf가 phone_only 기준 이상일 때만 허용
        phone_only_condition = phone_present and (phone_conf >= self.conf_th_phone_only)

        if self.state == "OFF":
            # (A) 빠른 ON
            if fast_condition:
                if self.fast_on_start is None:
                    self.fast_on_start = now
                if now - self.fast_on_start >= self.fast_on_hold_sec:
                    self.state = "ON"
                    self.fast_on_start = None
                    self.phone_only_on_start = None
                    self.off_start = None
            else:
                self.fast_on_start = None

            # (B) 폰만 있어도 ON (fallback)
            if self.state == "OFF" and phone_only_condition:
                if self.phone_only_on_start is None:
                    self.phone_only_on_start = now
                if now - self.phone_only_on_start >= self.phone_only_on_hold_sec:
                    self.state = "ON"
                    self.fast_on_start = None
                    self.phone_only_on_start = None
                    self.off_start = None
            else:
                self.phone_only_on_start = None

        else:  # ON
            # OFF는 보수적으로: 폰이 사라지거나, 손/고개 숙임 신호가 충분히 사라지면 OFF
            off_condition = (not phone_present) or (not (hand_near or head_down))

            if off_condition:
                if self.off_start is None:
                    self.off_start = now
                if now - self.off_start >= self.off_hold_sec:
                    self.state = "OFF"
                    self.fast_on_start = None
                    self.phone_only_on_start = None
                    self.off_start = None
            else:
                self.off_start = None

        return {
            "state": self.state,
            "phone_present": int(phone_present),
            "phone_conf": float(phone_conf),
            "hand_near": int(hand_near),
            "head_down": int(head_down),
            "head_ratio": self.head_ratio,
        }

    def draw(self, frame_bgr, result):
        if not self.draw_debug:
            return frame_bgr

        # 폰 박스
        if self.phone_box:
            x1, y1, x2, y2, conf = self.phone_box
            cv2.rectangle(frame_bgr, (x1, y1), (x2, y2), (0, 255, 0), 2)
            cv2.putText(
                frame_bgr,
                f"phone {conf:.2f}",
                (x1, max(0, y1 - 8)),
                cv2.FONT_HERSHEY_SIMPLEX,
                0.6,
                (0, 255, 0),
                2,
            )

        # 손 중심
        for (hx, hy) in self.hand_centers:
            cv2.circle(frame_bgr, (hx, hy), 6, (255, 255, 0), -1)

        # 상태 텍스트
        ratio_txt = "None" if result["head_ratio"] is None else f"{result['head_ratio']:.2f}"
        cv2.putText(
            frame_bgr,
            f"PHONE:{result['state']}  P:{result['phone_present']} conf:{result['phone_conf']:.2f} HN:{result['hand_near']} HD:{result['head_down']} ratio:{ratio_txt}",
            (20, 40),
            cv2.FONT_HERSHEY_SIMPLEX,
            0.7,
            (0, 255, 255),
            2,
        )

        return frame_bgr

