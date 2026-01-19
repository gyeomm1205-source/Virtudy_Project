import mediapipe as mp
import numpy as np
import time

class DrowsinessDetector:
    def __init__(self):
        # 설정값
        self.EAR_THRESHOLD = 0.19       # 눈 감김 기준값
        self.BLINK_THRESHOLD = 0.3      # [수정] 0.3초까지는 깜빡임으로 간주 (무시)
        self.SLEEP_TIME_LIMIT = 1.5     # 1.5초 이상 감으면 '졸음' 경고
        self.ABSENCE_TIME_LIMIT = 3.0   # 3초 이상 없으면 '자리 비움'
        
        # MediaPipe 초기화
        self.mp_face_mesh = mp.solutions.face_mesh
        self.face_mesh = self.mp_face_mesh.FaceMesh(
            max_num_faces=5,           # [수정] 최대 5명까지 탐지 (그 중 1명 선택)
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

    def process_frame(self, frame):
        h, w, _ = frame.shape
        rgb_frame = frame
        
        results = self.face_mesh.process(rgb_frame)
        
        # 1. 자리 비움 체크
        if not results.multi_face_landmarks:
            self.sleep_start_time = None
            if self.absence_start_time is None:
                self.absence_start_time = time.time()
            elif time.time() - self.absence_start_time > self.ABSENCE_TIME_LIMIT:
                return "ABSENT", 0.0, None
            return "NORMAL", 0.0, None

        # 2. 얼굴이 있다면 -> 가장 가까운(큰) 얼굴 하나 선택
        self.absence_start_time = None
        
        target_face_landmarks = None
        max_area = 0
        
        # 여러 얼굴 중 가장 크게 나온 얼굴 찾기
        for face_landmarks in results.multi_face_landmarks:
            # 얼굴의 위아래/좌우 랜드마크로 대략적인 크기 계산
            top = face_landmarks.landmark[10].y
            bottom = face_landmarks.landmark[152].y
            left = face_landmarks.landmark[234].x
            right = face_landmarks.landmark[454].x
            area = (bottom - top) * (right - left)
            
            if area > max_area:
                max_area = area
                target_face_landmarks = face_landmarks

        # 3. 선택된 얼굴로 졸음 분석
        landmarks = target_face_landmarks.landmark
        left_ear = self._calculate_ear(landmarks, self.LEFT_EYE, w, h)
        right_ear = self._calculate_ear(landmarks, self.RIGHT_EYE, w, h)
        current_ear = (left_ear + right_ear) / 2.0

        if current_ear < self.EAR_THRESHOLD:
            # 눈 감음 감지 시작
            if self.sleep_start_time is None:
                self.sleep_start_time = time.time()
            
            elapsed = time.time() - self.sleep_start_time
            
            if elapsed < self.BLINK_THRESHOLD:
                # 0.3초 미만이면 그냥 NORMAL (깜빡임 무시)
                self.status = "NORMAL"
            else:
                # 0.3초 넘어가면 졸음 경고
                self.status = "DROWSY"
        else:
            # 눈 뜸
            self.sleep_start_time = None
            self.status = "NORMAL"

        return self.status, current_ear, target_face_landmarks