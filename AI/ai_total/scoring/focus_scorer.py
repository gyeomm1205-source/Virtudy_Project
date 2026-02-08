# scoring/focus_scorer.py
import time
from core.types import FocusState, ScoreSnapshot
from core.config import Config

class FocusScorer:
<<<<<<< HEAD
    def __init__(self, initial_score: float = 100.0):
        if initial_score < 0:
            initial_score = 0.0
        if initial_score > 100:
            initial_score = 100.0
        self.score = float(initial_score)
=======
    def __init__(self):
        self.score = 100.0
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
        self.last_t = time.time()

        self.focused_sec = 0.0
        self.drowsy_sec = 0.0
        self.phone_sec = 0.0
        self.absent_sec = 0.0

        self.penalty = {
            FocusState.FOCUSED: Config.PENALTY_FOCUSED,
            FocusState.DROWSY: Config.PENALTY_DROWSY,
            FocusState.PHONE: Config.PENALTY_PHONE,
            FocusState.ABSENT: Config.PENALTY_ABSENT,
            FocusState.UNKNOWN: Config.PENALTY_UNKNOWN,
        }

    def update(self, state: FocusState) -> ScoreSnapshot:
        now = time.time()
        dt = now - self.last_t
        self.last_t = now
        
        # Prevent huge dt jumps (e.g. debugging/pause)
        if dt > 1.0:
<<<<<<< HEAD
            dt = 1.0
        rate_scale = Config.SCORE_RATE_SCALE
=======
            dt = 0.1
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186

        # Accumulate time
        if state == FocusState.FOCUSED:
            self.focused_sec += dt
            # [NEW] Add reward for focusing
<<<<<<< HEAD
            self.score += Config.REWARD_FOCUSED * dt * rate_scale
=======
            self.score += Config.REWARD_FOCUSED * dt
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
        elif state == FocusState.DROWSY:
            self.drowsy_sec += dt
        elif state == FocusState.PHONE:
            self.phone_sec += dt
        elif state == FocusState.ABSENT:
            self.absent_sec += dt

        # Apply penalty
        p = self.penalty.get(state, 0.0)
<<<<<<< HEAD
        self.score -= p * dt * rate_scale
=======
        self.score -= p * dt
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
        
        # Cap score
        if self.score < 0:
            self.score = 0.0
        if self.score > 100:
            self.score = 100.0

        return ScoreSnapshot(
            score=self.score,
            focused_sec=self.focused_sec,
            drowsy_sec=self.drowsy_sec,
            phone_sec=self.phone_sec,
            absent_sec=self.absent_sec,
        )
