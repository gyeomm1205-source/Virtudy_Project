# fusion/state_fuser.py
from core.types import FrameSignals, FocusDecision, FocusState
from core.config import Config

class StateFuser:
    def __init__(self):
        self.last_state = FocusState.FOCUSED
        self.state_count = 0
        self.STICKY_THRESHOLD = 10 # [Tuned] Reduced 15 -> 10 for faster recovery (~1s at 10fps)

    def decide(self, sig: FrameSignals) -> FocusDecision:
        # Determine raw candidate state
        candidate = FocusState.FOCUSED
        reason = "Normal"
        confidence = 0.8

        # 1) PHONE (Highest Priority violation)
        # Only trigger PHONE on actual in-use (presence alone shouldn't lock state)
        if sig.phone.phone_in_use:
            candidate = FocusState.PHONE
            reason = f"Phone in use (Cell={sig.phone.is_cell_phone})"
            confidence = sig.phone.phone_in_use_score

        # 2) DROWSY (Priority violation)
        # If strong phone evidence + head down, prefer PHONE over SLEEP.
        elif sig.drowsy.drowsy_score >= Config.DROWSY_TRIGGER_SCORE:
            phone_override = (
                sig.phone.is_cell_phone
                and sig.phone.phone_conf >= Config.PHONE_CONFIRMED_TH
                and sig.drowsy.head_pitch is not None
                and sig.drowsy.head_pitch > Config.PITCH_PHONE_USE_TH
            )
            if phone_override:
                candidate = FocusState.PHONE
                reason = "Phone present + head down (override drowsy)"
                confidence = max(sig.phone.phone_conf, sig.drowsy.drowsy_score)
            else:
                candidate = FocusState.DROWSY
                reason = "Eyes closed (No true phone present)"
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
