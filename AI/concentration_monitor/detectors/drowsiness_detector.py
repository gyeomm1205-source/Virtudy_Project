# detectors/drowsiness_detector.py
from typing import Optional
import time
from core.types import DrowsinessSignal
from utils.smoothing import BoolWindow
from core.config import Config

class DrowsinessDetector:
    def __init__(self):
        self.drowsy_smoother = BoolWindow(Config.DROWSY_WINDOW, Config.DROWSY_TRUE_RATIO)
        
        # [NEW] Adaptive Calibration
        self.calibration_sum = 0.0
        self.calibration_count = 0
        self.calibration_duration = 30.0  # seconds
        self.calibration_start_time = None
        self.baseline_ear = None
        self.adaptive_drowsy_th = Config.EAR_DROWSY_TH  # Fallback to default
        self.calibration_complete = False
        self.closed_frames = 0
        self.closed_start_time = None
        self.last_signal = DrowsinessSignal(face_detected=False, ear=None, head_pitch=None, drowsy_score=0.0, current_threshold=Config.EAR_DROWSY_TH)

    def process(self, face_detected: bool, ear: Optional[float], head_pitch: Optional[float], face_updated: bool = True) -> DrowsinessSignal:
        if not face_updated:
            return self.last_signal
        if not face_detected or ear is None or head_pitch is None:
            # If face not detected, we cannot judge drowsiness here (Absence will handle it)
            self.last_signal = DrowsinessSignal(face_detected=face_detected, ear=ear, head_pitch=head_pitch, drowsy_score=0.0, current_threshold=self.adaptive_drowsy_th)
            return self.last_signal

        # [NEW] Adaptive Calibration Logic
        now = time.time()
        if self.calibration_start_time is None:
            self.calibration_start_time = now
            print("[INFO] Starting EAR Calibration (30s)...")

        if not self.calibration_complete:
            elapsed = now - self.calibration_start_time
            if elapsed < self.calibration_duration:
                # Still collecting baseline samples (assume user is focused/awake)
                self.calibration_sum += ear
                self.calibration_count += 1
                if self.calibration_count % 50 == 0:
                    print(f"[DEBUG] Calibrating... {elapsed:.1f}/30.0s (Samples: {self.calibration_count})")
                
                # Update baseline incrementally for responsiveness
                self.baseline_ear = self.calibration_sum / self.calibration_count
                self.adaptive_drowsy_th = self.baseline_ear * 0.65  # 65% of baseline EAR
            else:
                self.calibration_complete = True
                self.baseline_ear = self.calibration_sum / self.calibration_count
                self.adaptive_drowsy_th = self.baseline_ear * 0.65
                print(f"[SUCCESS] Calibration Complete! Baseline EAR: {self.baseline_ear:.3f}, Adaptive Th: {self.adaptive_drowsy_th:.3f}")

        # Use adaptive threshold instead of static Config value
        current_th = self.adaptive_drowsy_th
        awake_th = self.baseline_ear * 0.85 if self.baseline_ear else Config.EAR_AWAKE_TH

        # Logic: Low EAR (Eyes closed) AND Head dropping (Pitch > Threshold)
        # Note: Depending on user, sometimes just EAR is enough, or just Pitch.
        # Here we use specific combination or strong individual signals.
        # For now, let's Stick to the plan: EAR < Thresh AND Pitch > Thresh.
        # But wait, looking down is also a sign of phone use.
        # So we might want to rely more on EAR for drowsiness.
        
        # Let's say drowsiness is mainly "Eyes Closed" (EAR).
        # "Head Drop" is a reinforcing factor.
        
        eyes_closed = ear < current_th
        head_dropped = head_pitch > Config.PITCH_DROWSY_TH
        
        # Drowsy Candidate: Eyes closed OR (Eyes somewhat closed AND Head dropped)
        # For simplicity and robustness as discussed:
        # Drowsy = Eyes Closed. 
        # (Head drop is ambiguous with phone use, but phone use requires phone object)
        
        if eyes_closed:
            self.closed_frames += 1
            if self.closed_start_time is None:
                self.closed_start_time = now
        else:
            self.closed_frames = 0
            self.closed_start_time = None

        closed_long_enough = False
        if self.closed_start_time is not None:
            closed_long_enough = (now - self.closed_start_time) >= Config.DROWSY_MIN_CLOSED_SEC

        drowsy_candidate = eyes_closed and self.closed_frames >= Config.DROWSY_MIN_CLOSED_FRAMES and closed_long_enough
        
        # If head is heavily dropped, we might also consider it drowsiness if phone is NOT present?
        # But we don't know about phone here.
        # Let's stick to simple EAR check for now, or EAR + Head.
        
        # Rationale: Phone use is detected via phone object + behavior,
        # so drowsiness should prioritize eyes-closed signals.
        
        if head_dropped and ear < (current_th * 1.2) and closed_long_enough:
             # If head is dropped, we tolerate slightly larger EAR (eyes purely looking down)
             drowsy_candidate = True

        # [NEW] Fast Awake: If eyes are wide open, clear the buffer to wake up immediately
        if ear >= awake_th:
            self.drowsy_smoother.q.clear()
            drowsy_candidate = False

        drowsy = self.drowsy_smoother.update(drowsy_candidate)
        
        # Score: 1.0 if confirmed, 0.6 if candidate
        score = 1.0 if drowsy else (0.6 if drowsy_candidate else 0.0)

        self.last_signal = DrowsinessSignal(
            face_detected=face_detected,
            ear=ear,
            head_pitch=head_pitch,
            drowsy_score=score,
            current_threshold=current_th
        )
        return self.last_signal
