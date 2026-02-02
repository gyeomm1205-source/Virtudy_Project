# fusion/state_fuser.py
from core.types import FrameSignals, FocusDecision, FocusState

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
        # [Fix] Only Mute Drowsiness if:
        # - It is clearly a CELL PHONE (Class 67)
        # - OR the behavior detector is already confident (PhoneDetector ON)
        # We NO LONGER mute for stationary laptops/remotes (Phantom Phone)
        is_truly_phone = sig.phone.is_cell_phone or sig.phone.phone_in_use
        
        if is_truly_phone:
            candidate = FocusState.PHONE
            reason = f"Phone detected (Cell={sig.phone.is_cell_phone}, InUse={sig.phone.phone_in_use})"
            confidence = sig.phone.phone_in_use_score if sig.phone.phone_in_use else 0.5
            if sig.phone.phone_conf > 0.4 and sig.phone.is_cell_phone:
                confidence = max(confidence, 0.8)

        # 2) DROWSY (Priority violation)
        # If it's just a laptop (Phantom) and eyes are closed, we prioritize DROWSY again.
        elif not is_truly_phone and sig.drowsy.drowsy_score >= 0.6:
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
