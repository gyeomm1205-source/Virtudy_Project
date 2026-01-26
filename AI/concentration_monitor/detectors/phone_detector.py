# detectors/phone_detector.py
from typing import Optional
from core.types import PhoneSignal
from utils.smoothing import BoolWindow
from core.config import Config

class PhoneDetector:
    def __init__(self):
        self.use_smoother = BoolWindow(Config.PHONE_USE_WINDOW, Config.PHONE_USE_TRUE_RATIO)

    def process(
        self,
        phone_conf: float,          # YOLO conf (0.0 if not found)
        face_detected: bool,
        head_pitch: Optional[float],
        hand_interaction: bool      # Hand near phone?
    ) -> PhoneSignal:
        """
        Determines if phone is being *used*.
        Criteria:
        1. Phone Object Detected (High confidence)
        AND
        2. Behavior detected:
           - Head looking down OR
           - Hand interacting with it
        """
        phone_present = phone_conf >= Config.PHONE_PRESENT_TH

        # Behavior check
        looking_down = (head_pitch is not None and head_pitch > Config.PITCH_PHONE_USE_TH)
        
        # Use candidate: Phone present AND (Looking down OR Hand interacting)
        # Note: If we just have phone on desk but looking straight ahead -> Not usage.
        use_candidate = False
        if phone_present:
            # If confidence is moderate-high (0.4+), assume usage if user is present.
            # (Phone on desk usually has very stable but lower confidence or is excluded by logic if we tracked movement)
            # But for now, if it's 0.4+ and face is there, it's likely a distraction.
            if phone_conf > 0.4:
                 use_candidate = True
            elif face_detected and (looking_down or hand_interaction):
                use_candidate = True
        
        # If phone is VERY close to face (not easily measurable without depth/specific landmarks, 
        # but 'hand_interaction' might capture holding phone near face).
        
        phone_in_use = self.use_smoother.update(use_candidate)

        # Score calculation
        score = 1.0 if phone_in_use else (0.6 if use_candidate else 0.0)

        return PhoneSignal(
            phone_present=phone_present,
            phone_conf=phone_conf,
            phone_in_use_score=score,
            phone_in_use=phone_in_use
        )
