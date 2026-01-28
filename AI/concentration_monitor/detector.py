#detector.py
import mediapipe as mp
import numpy as np
import time
import cv2

# 클래스 이름을 run.py가 찾는 'ConcentrationDetector'로 변경합니다.
class ConcentrationDetector:
    def __init__(self):
        # 설정값
        self.EAR_THRESHOLD = 0.19
        self.BLINK_THRESHOLD = 0.3
        self.SLEEP_TIME_LIMIT = 1.5
        self.ABSENCE_TIME_LIMIT = 3.0
        
        # MediaPipe 초기화
        self.mp_face_mesh = mp.solutions.face_mesh
        self.face_mesh = self.mp_face_mesh.FaceMesh(
            max_num_faces=1, # 메인 사용자를 위해 1명으로 설정 권장
            refine_landmarks=True,
            min_detection_confidence=0.5,
            min_tracking_confidence=0.5
        )
        
        # 상태 변수
        self.sleep_start_time = None
        self.absence_start_time = None
        self.status = "NORMAL"

        # 눈 좌표 인덱스
        self.LEFT_EYE = [33, 160, 158, 133, 153, 144]
        self.RIGHT_EYE = [362, 385, 387, 263, 373, 380]

    def _calculate_ear(self, landmarks, indices, w, h):
        points = []
        for idx in indices:
            lm = landmarks[idx]
            points.append(np.array([lm.x * w, lm.y * h]))
        
        v1 = np.linalg.norm(points[1] - points[5])
        v2 = np.linalg.norm(points[2] - points[4])
        h_dist = np.linalg.norm(points[0] - points[3])
        return (v1 + v2) / (2.0 * h_dist)

    # run.py 하단 루프에서 호출하는 메서드 이름으로 맞춤
    def get_current_state(self, frame):
        """기존 process_frame의 로직을 수행하고 현재 상태 문자열을 반환합니다."""
        if frame is None:
            return "UNKNOWN"

        h, w, _ = frame.shape
        results = self.face_mesh.process(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
        
        # 1. 자리 비움 체크
        if not results.multi_face_landmarks:
            self.sleep_start_time = None
            if self.absence_start_time is None:
                self.absence_start_time = time.time()
            elif time.time() - self.absence_start_time > self.ABSENCE_TIME_LIMIT:
                return "ABSENT"
            return "NORMAL"

        # 2. 얼굴이 있다면 -> 자리 비움 시간 초기화
        self.absence_start_time = None
        target_face_landmarks = results.multi_face_landmarks[0] # 첫 번째 얼굴 선택

        # 3. 졸음 분석
        landmarks = target_face_landmarks.landmark
        left_ear = self._calculate_ear(landmarks, self.LEFT_EYE, w, h)
        right_ear = self._calculate_ear(landmarks, self.RIGHT_EYE, w, h)
        current_ear = (left_ear + right_ear) / 2.0

        if current_ear < self.EAR_THRESHOLD:
            if self.sleep_start_time is None:
                self.sleep_start_time = time.time()
            
            elapsed = time.time() - self.sleep_start_time
            if elapsed >= self.BLINK_THRESHOLD:
                self.status = "DROWSY"
        else:
            self.sleep_start_time = None
            self.status = "FOCUSED" # 'NORMAL' 대신 집중 상태인 'FOCUSED' 반환

        return self.status