# detectors/drowsiness_detector.py
from typing import Optional
from core.types import DrowsinessSignal
from utils.smoothing import BoolWindow
from core.config import Config

class DrowsinessDetector:
    def __init__(self):
        self.drowsy_smoother = BoolWindow(Config.DROWSY_WINDOW, Config.DROWSY_TRUE_RATIO)

    def process(self, face_detected: bool, ear: Optional[float], head_pitch: Optional[float]) -> DrowsinessSignal:
        if not face_detected or ear is None or head_pitch is None:
            # If face not detected, we cannot judge drowsiness here (Absence will handle it)
            return DrowsinessSignal(face_detected=face_detected, ear=ear, head_pitch=head_pitch, drowsy_score=0.0)

        # Logic: Low EAR (Eyes closed) AND Head dropping (Pitch > Threshold)
        # Note: Depending on user, sometimes just EAR is enough, or just Pitch.
        # Here we use specific combination or strong individual signals.
        # For now, let's Stick to the plan: EAR < Thresh AND Pitch > Thresh.
        # But wait, looking down is also a sign of phone use.
        # So we might want to rely more on EAR for drowsiness.
        
        # Let's say drowsiness is mainly "Eyes Closed" (EAR).
        # "Head Drop" is a reinforcing factor.
        
        eyes_closed = ear < Config.EAR_DROWSY_TH
        head_dropped = head_pitch > Config.PITCH_DROWSY_TH
        
        # Drowsy Candidate: Eyes closed OR (Eyes somewhat closed AND Head dropped)
        # For simplicity and robustness as discussed:
        # Drowsy = Eyes Closed. 
        # (Head drop is ambiguous with phone use, but phone use requires phone object)
        
        drowsy_candidate = eyes_closed
        
        # If head is heavily dropped, we might also consider it drowsiness if phone is NOT present?
        # But we don't know about phone here.
        # Let's stick to simple EAR check for now, or EAR + Head.
        
        # Original user request: "시선이 아래로 내려가는 상황과 졸아서 눈이 내려간 상황을 구분하기가 어렵다"
        # Proposed solution: Phone detector uses Phone Object + Gaze.
        # Drowsiness detector should prioritize "Eyes Closed".
        
        if head_dropped and ear < (Config.EAR_DROWSY_TH * 1.2):
             # If head is dropped, we tolerate slightly larger EAR (eyes purely looking down)
             drowsy_candidate = True

        # [NEW] Fast Awake: If eyes are wide open, clear the buffer to wake up immediately
        if ear >= Config.EAR_AWAKE_TH:
            self.drowsy_smoother.q.clear()
            drowsy_candidate = False

        drowsy = self.drowsy_smoother.update(drowsy_candidate)
        
        # Score: 1.0 if confirmed, 0.6 if candidate
        score = 1.0 if drowsy else (0.6 if drowsy_candidate else 0.0)

        return DrowsinessSignal(
            face_detected=face_detected,
            ear=ear,
            head_pitch=head_pitch,
            drowsy_score=score
        )
