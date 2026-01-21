import cv2
import numpy as np
from detector import DrowsinessDetector

# ===== 폰 감지 기능만 옵션으로 추가 =====
ENABLE_PHONE_DETECTOR = True      # 필요 없으면 False
PHONE_DRAW_DEBUG = False          # True면 phone_detector 내부 디버그(박스/점/텍스트) 그려짐
SHOW_PHONE_TEXT_ON_BAR = True     # 상단바 텍스트에 Phone: ON/OFF 표시만 할지


def main():
    detector = DrowsinessDetector()
    cap = cv2.VideoCapture(0)

    # 폰 감지기 로드 (실패해도 기존 기능은 동작하도록)
    phone_detector = None
    if ENABLE_PHONE_DETECTOR:
        try:
            from phone_detector import PhoneDetector
            phone_detector = PhoneDetector(draw_debug=PHONE_DRAW_DEBUG)
        except Exception as e:
            print("[WARN] PhoneDetector 로드 실패:", e)
            phone_detector = None

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
            # 10:이마, 152:턱, 234:왼쪽볼, 454:오른쪽볼
            top = int(target_landmarks.landmark[10].y * h)
            bottom = int(target_landmarks.landmark[152].y * h)
            left = int(target_landmarks.landmark[234].x * w)
            right = int(target_landmarks.landmark[454].x * w)

            margin = 30
            top = max(0, top - margin * 2)
            bottom = min(h, bottom + margin)
            left = max(0, left - margin)
            right = min(w, right + margin)

            box_color = (100, 255, 100)

            if status == "DROWSY":
                box_color = (0, 0, 255)

            cv2.rectangle(frame, (left, top), (right, bottom), box_color, 2)
            cv2.putText(frame, "Target", (left, top - 10),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.6, box_color, 2)

        # --- [2] 상태 텍스트 표시 ---
        msg = f"Status: {status} (EAR: {ear_value:.2f})"

        if status == "NORMAL":
            cv2.rectangle(frame, (0, 0), (640, 40), (50, 200, 50), -1)

        elif status == "DROWSY":
            cv2.rectangle(frame, (0, 0), (640, 40), (0, 0, 255), -1)
            cv2.putText(frame, "WAKE UP!!!", (50, 200),
                        cv2.FONT_HERSHEY_SIMPLEX, 2, (0, 0, 255), 5)

        elif status == "ABSENT":
            cv2.rectangle(frame, (0, 0), (640, 40), (255, 0, 0), -1)
            cv2.putText(frame, "ABSENT!!!", (50, 200),
                        cv2.FONT_HERSHEY_SIMPLEX, 2, (255, 0, 0), 5)

        # ===== 여기부터 "폰 감지"만 최소로 추가 =====
        phone_state = None

        if phone_detector is not None:
            try:
                phone_result = phone_detector.update(frame)  # BGR frame
                phone_state = phone_result.get("state", None)

                # 디버그 그리기(원할 때만)
                if PHONE_DRAW_DEBUG:
                    frame = phone_detector.draw(frame, phone_result)

            except Exception as e:
                # 폰 감지 실패해도 기존 기능 영향 없게
                print("[WARN] phone_detector.update 실패:", e)
                phone_state = None

        # 상단바 텍스트: 기존 msg 유지 + (옵션) 폰 상태만 덧붙이기
        bar_text = msg
        if SHOW_PHONE_TEXT_ON_BAR and phone_state is not None:
            bar_text += f" | Phone: {phone_state}"

        cv2.putText(frame, bar_text, (10, 30),
                    cv2.FONT_HERSHEY_SIMPLEX, 0.7, (255, 255, 255), 2)

        cv2.imshow('Smart Sleep Monitor', frame)

        if cv2.waitKey(5) & 0xFF == ord('q'):
            break

    # 정리
    if phone_detector is not None:
        try:
            phone_detector.close()
        except Exception:
            pass

    cap.release()
    cv2.destroyAllWindows()


if __name__ == "__main__":
    main()
