# fusion/state_fuser.py
from typing import Optional

from core.types import FrameSignals, FocusDecision, FocusState
from core.config import Config

class StateFuser:
    def __init__(self):
<<<<<<< HEAD
        # Frame-based hold derived from existing 판단 프레임 설정
        self.hold_frames = {
            FocusState.ABSENT: Config.ABSENT_WINDOW,
            FocusState.DROWSY: Config.DROWSY_WINDOW,
            FocusState.PHONE: Config.PHONE_USE_WINDOW,
        }
        # FOCUS 복귀는 가장 보수적인 판단 프레임 기준으로 유지
        self.focus_recover_frames = min(
            Config.ABSENT_WINDOW,
            Config.DROWSY_WINDOW,
            Config.PHONE_USE_WINDOW,
        )
        self.tick = 0
        self.last_state = None
        self.last_change_tick = 0
        self.last_decision: Optional[FocusDecision] = None
        self.focus_streak = 0

    def _raw_decide(self, sig: FrameSignals) -> FocusDecision:
        # Priority: PHONE > ABSENT > DROWSY > FOCUSED

        # 1) PHONE (Prioritize over Drowsy because looking down + phone = Phone Use)
=======
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
>>>>>>> 1d12e087b06e8ccc4de00953fd963920a5f14c00
        if sig.phone.phone_in_use:
            candidate = FocusState.PHONE
            reason = f"Phone in use (Cell={sig.phone.is_cell_phone})"
            confidence = sig.phone.phone_in_use_score

<<<<<<< HEAD
        # 2) ABSENT
        if sig.absent.absent:
            return FocusDecision(FocusState.ABSENT, 1.0, "Face missing")

        # 3) DROWSY
        if sig.drowsy.drowsy_score >= 0.9:
            return FocusDecision(FocusState.DROWSY, sig.drowsy.drowsy_score, "Eyes closed")
=======
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
>>>>>>> 1d12e087b06e8ccc4de00953fd963920a5f14c00

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

<<<<<<< HEAD
        return FocusDecision(FocusState.UNKNOWN, 0.0, "Insufficient signals")

    def _switch(self, decision: FocusDecision) -> FocusDecision:
        self.last_state = decision.state
        self.last_decision = decision
        self.last_change_tick = self.tick
        return decision

    def decide(self, sig: FrameSignals) -> FocusDecision:
        self.tick += 1
        raw = self._raw_decide(sig)
        raw_state = raw.state

        if self.last_decision is None:
            return self._switch(raw)

        # UNKNOWN은 상태 변경 없이 유지
        if raw_state == FocusState.UNKNOWN:
            return self.last_decision

        if raw_state == FocusState.FOCUSED:
            self.focus_streak += 1
        else:
            self.focus_streak = 0

        # 현재 상태가 FOCUS면 바로 비포커스 전환 허용
        if self.last_state == FocusState.FOCUSED:
            if raw_state == FocusState.FOCUSED:
                self.last_decision = raw
                return raw
            return self._switch(raw)

        # 현재 상태가 비포커스인 경우: 최소 유지 프레임 충족 전에는 유지
        hold = self.hold_frames.get(self.last_state, 0)
        held = (self.tick - self.last_change_tick) < hold

        if raw_state == FocusState.FOCUSED:
            if held or self.focus_streak < self.focus_recover_frames:
                return self.last_decision
            return self._switch(raw)

        # 비포커스 간 전환
        if raw_state == self.last_state:
            self.last_decision = raw
            return self.last_decision

        if held:
            return self.last_decision

        return self._switch(raw)
=======
        return FocusDecision(candidate, confidence, reason)
>>>>>>> 1d12e087b06e8ccc4de00953fd963920a5f14c00
