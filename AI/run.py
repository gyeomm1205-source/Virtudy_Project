import cv2
import numpy as np
from detector import DrowsinessDetector

def main():
    detector = DrowsinessDetector()
    cap = cv2.VideoCapture(0)
    
    print("시스템 시작! (종료: q)")

    while cap.isOpened():
        ret, frame = cap.read()
        if not ret:
            break

        frame.flags.writeable = False
        rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
        h, w, _ = frame.shape
        
        # 감지기 실행
        status, ear_value, target_landmarks = detector.process_frame(rgb_frame)
        
        frame.flags.writeable = True
        
        # --- [1] 인식된 얼굴 표시 (무서운 그물망 제거 -> 심플한 박스) ---
        if target_landmarks:
            # 얼굴의 상하좌우 끝점 좌표를 가져와서 박스 크기 계산
            # 10:이마, 152:턱, 234:왼쪽볼, 454:오른쪽볼
            top = int(target_landmarks.landmark[10].y * h)
            bottom = int(target_landmarks.landmark[152].y * h)
            left = int(target_landmarks.landmark[234].x * w)
            right = int(target_landmarks.landmark[454].x * w)
            
            # 박스 여백을 조금 줘서 여유롭게 그리기
            margin = 30
            top = max(0, top - margin * 2) # 이마 위는 머리 때문에 여백을 더 줌
            bottom = min(h, bottom + margin)
            left = max(0, left - margin)
            right = min(w, right + margin)

            # 박스 그리기 (기본: 민트색)
            box_color = (100, 255, 100) 
            
            if status == "DROWSY":
                box_color = (0, 0, 255) # 졸음 시 빨간 박스
            
            # 네모 박스 그리기
            cv2.rectangle(frame, (left, top), (right, bottom), box_color, 2)
            
            # 박스 위에 'Target' 글씨 쓰기
            cv2.putText(frame, "Target", (left, top - 10), 
                        cv2.FONT_HERSHEY_SIMPLEX, 0.6, box_color, 2)

        # --- [2] 상태 텍스트 표시 ---
        msg = f"Status: {status} (EAR: {ear_value:.2f})"
        
        if status == "NORMAL":
            cv2.rectangle(frame, (0,0), (640, 40), (50, 200, 50), -1)
            
        elif status == "DROWSY":
            cv2.rectangle(frame, (0,0), (640, 40), (0, 0, 255), -1)
            cv2.putText(frame, "WAKE UP!!!", (50, 200), cv2.FONT_HERSHEY_SIMPLEX, 2, (0, 0, 255), 5)
            
        elif status == "ABSENT":
            cv2.rectangle(frame, (0,0), (640, 40), (255, 0, 0), -1)
            cv2.putText(frame, "ABSENT!!!", (50, 200), cv2.FONT_HERSHEY_SIMPLEX, 2, (255, 0, 0), 5)

        # 상단 정보바 텍스트
        cv2.putText(frame, msg, (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (255, 255, 255), 2)

        cv2.imshow('Smart Sleep Monitor', frame)
        
        if cv2.waitKey(5) & 0xFF == ord('q'):
            break

    cap.release()
    cv2.destroyAllWindows()

if __name__ == "__main__":
    main()