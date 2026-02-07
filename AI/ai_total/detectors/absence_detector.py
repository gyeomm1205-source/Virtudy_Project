# detectors/absence_detector.py
import time

from core.types import AbsenceSignal
from utils.smoothing import BoolWindow
from core.config import Config

class AbsenceDetector:
    def __init__(self):
        self.absent_smoother = BoolWindow(Config.ABSENT_WINDOW, Config.ABSENT_TRUE_RATIO)
        self.last_face_ts = time.time()
        self.grace_sec = Config.ABSENT_GRACE_SEC

    def process(self, face_present: bool, phone_present: bool = False) -> AbsenceSignal:
        # face_present=False means candidate for absence
        now = time.time()
        if face_present:
            self.last_face_ts = now

        face_missing = (not face_present) and (now - self.last_face_ts >= self.grace_sec)
        # Absent only if face missing AND no phone present
        absent_now = face_missing and (not phone_present)
        absent = self.absent_smoother.update(absent_now)

        return AbsenceSignal(
            face_detected=face_present,
            absent_score=1.0 if absent_now else 0.0,
            absent=absent
        )
