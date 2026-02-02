# core/types.py
from dataclasses import dataclass
from enum import Enum, auto
from typing import Optional

class FocusState(Enum):
    UNKNOWN = auto()
    FOCUSED = auto()
    DROWSY = auto()  # Maps to SLEEP
    PHONE = auto()
    ABSENT = auto()  # Maps to AWAY

    def to_backend_event_type(self) -> str:
        """
        Backend StudyEventType mapping:
        FOCUS, SLEEP, PHONE, AWAY
        """
        if self == FocusState.FOCUSED:
            return "FOCUS"
        elif self == FocusState.DROWSY:
            return "SLEEP"
        elif self == FocusState.PHONE:
            return "PHONE"
        elif self == FocusState.ABSENT:
            return "AWAY"
        else:
            return "FOCUS" # Default fallback? Or maybe handle UNKNOWN differently. 
                           # For now, let's treat UNKNOWN as FOCUS or ignore it, 
                           # but FOCUS is a safe safe default if we must send something.

@dataclass
class DrowsinessSignal:
    face_detected: bool
    ear: Optional[float] = None
    head_pitch: Optional[float] = None   # +ve means looking down
    drowsy_score: float = 0.0            # 0.0 ~ 1.0

@dataclass
class AbsenceSignal:
    face_detected: bool
    absent_score: float = 0.0            # 0.0 ~ 1.0
    absent: bool = False                 # True if absent condition met (after smoothing)

@dataclass
class PhoneSignal:
    phone_present: bool
    phone_conf: float = 0.0
    phone_in_use_score: float = 0.0      # 0.0 ~ 1.0 (phone + behavior score)
    phone_in_use: bool = False           # True if phone usage condition met

@dataclass
class FrameSignals:
    drowsy: DrowsinessSignal
    absent: AbsenceSignal
    phone: PhoneSignal

@dataclass
class FocusDecision:
    state: FocusState
    confidence: float
    reason: str

@dataclass
class ScoreSnapshot:
    score: float
    focused_sec: float
    drowsy_sec: float
    phone_sec: float
    absent_sec: float
