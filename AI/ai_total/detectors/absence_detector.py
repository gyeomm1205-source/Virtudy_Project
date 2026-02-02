# detectors/absence_detector.py
from core.types import AbsenceSignal
from utils.smoothing import BoolWindow
from core.config import Config

class AbsenceDetector:
    def __init__(self):
        self.absent_smoother = BoolWindow(Config.ABSENT_WINDOW, Config.ABSENT_TRUE_RATIO)

    def process(self, face_detected: bool) -> AbsenceSignal:
        # face_detected=False means candidate for absence
        absent_now = not face_detected
        absent = self.absent_smoother.update(absent_now)

        return AbsenceSignal(
            face_detected=face_detected,
            absent_score=1.0 if absent_now else 0.0,
            absent=absent
        )
