# fusion/state_fuser.py
from core.types import FrameSignals, FocusDecision, FocusState

class StateFuser:
    def decide(self, sig: FrameSignals) -> FocusDecision:
        # Priority: ABSENT > DROWSY > PHONE > FOCUSED
        
        # 1) ABSENT
        if sig.absent.absent:
            return FocusDecision(FocusState.ABSENT, 1.0, "Face missing")

        # 2) PHONE (Prioritize over Drowsy because looking down + phone = Phone Use)
        if sig.phone.phone_in_use:
            return FocusDecision(FocusState.PHONE, sig.phone.phone_in_use_score, "Phone use detected")

        # 3) DROWSY
        if sig.drowsy.drowsy_score >= 0.9:
            return FocusDecision(FocusState.DROWSY, sig.drowsy.drowsy_score, "Eyes closed")

        # 4) FOCUSED
        # If face is detected and no other negative states
        if sig.drowsy.face_detected:
            return FocusDecision(FocusState.FOCUSED, 0.8, "Normal")

        return FocusDecision(FocusState.UNKNOWN, 0.0, "Insufficient signals")
