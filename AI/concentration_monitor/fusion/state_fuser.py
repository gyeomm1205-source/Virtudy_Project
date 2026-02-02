# fusion/state_fuser.py
from core.types import FrameSignals, FocusDecision, FocusState

class StateFuser:
    def __init__(self):
        self.last_state = FocusState.FOCUSED
        self.state_count = 0
        self.STICKY_THRESHOLD = 15 # Frames to 'stick' to a violation state

    def decide(self, sig: FrameSignals) -> FocusDecision:
        # Determine raw candidate state
        candidate = FocusState.FOCUSED
        reason = "Normal"
        confidence = 0.8

        # 1) PHONE (Highest Priority violation)
        if sig.phone.phone_in_use:
            candidate = FocusState.PHONE
            reason = "Phone use detected"
            confidence = sig.phone.phone_in_use_score

        # 2) DROWSY (Priority violation)
        elif sig.drowsy.drowsy_score >= 0.9:
            candidate = FocusState.DROWSY
            reason = "Eyes closed"
            confidence = sig.drowsy.drowsy_score
            
        # 3) ABSENT
        elif sig.absent.absent:
            candidate = FocusState.ABSENT
            reason = "Face missing"
            confidence = 1.0

        # --- Stability Logic (Hysteresis) ---
        # If we are in a violation state (PHONE/SLEEP/AWAY), 
        # don't flicker back to FOCUS unless we see it consistently.
        if self.last_state in [FocusState.PHONE, FocusState.DROWSY, FocusState.ABSENT]:
            if candidate == FocusState.FOCUSED:
                self.state_count += 1
                if self.state_count < self.STICKY_THRESHOLD:
                    # Stick to last violation
                    return FocusDecision(self.last_state, 0.7, f"Stable {self.last_state.name} (Hysteresis)")
            else:
                self.state_count = 0 # Detected violation again, reset counter
        
        self.last_state = candidate
        if candidate == FocusState.FOCUSED:
             self.state_count = 0

        return FocusDecision(candidate, confidence, reason)
