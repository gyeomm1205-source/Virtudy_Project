import time
from typing import Optional
from core.types import PhoneSignal
from core.config import Config

class PhoneDetector:
    def __init__(self):
        # State Machine Variables (Ported from root phone_detector.py)
        self.state = "OFF"
        self.fast_on_start = None
        self.phone_only_on_start = None
        self.off_start = None

        self._last_debug_time = 0.0
        self._last_debug_state = None
        
        # Configuration (Using centralized Config but logic from root)
        self.fast_on_hold_sec = 0.2
        self.phone_only_on_hold_sec = 0.1 # Reduced 0.3 -> 0.1 for faster detection
        self.off_hold_sec = 0.5    # Reduced from 1.5 to 0.5 for faster OFF transition
        # self.conf_th_phone_only removed (logic moved to process() with dual thresholds)

    def process(
        self,
        phone_conf: float,          
        face_detected: bool,
        head_pitch: Optional[float],
        hand_interaction: bool     
    ) -> PhoneSignal:
        
        # 1. Inputs
        # [NEW] Dual Threshold Logic
        # Candidate: Weakly detected (Needs corroboration like hand or head down)
        phone_candidate = phone_conf >= Config.PHONE_CANDIDATE_TH
        
        # Confirmed: Strongly detected (Standalone)
        phone_confirmed = phone_conf >= Config.PHONE_CONFIRMED_TH
        
        looking_down = (head_pitch is not None and head_pitch > Config.PITCH_PHONE_USE_TH)
        
        # 2. Logic Conditions
        face_missing = not face_detected
        # (A) Fast Condition: Candidate + (Hand or Head Down)
        fast_condition = phone_candidate and (hand_interaction or looking_down)
        # (B) Face Occlusion: Confirmed phone with missing face
        occlusion_condition = face_missing and phone_confirmed
        
        # (B) Phone Only Condition: Confirmed Phone
        phone_only_condition = phone_confirmed
        
        # [DEBUG] Internal State Print (rate-limited)
        if phone_candidate:
           now = time.time()
           if (self._last_debug_state != self.state) or (now - self._last_debug_time >= 1.0):
               print(f"[DEBUG-PHONE] Conf={phone_conf:.2f} (Cand={phone_candidate}, Confirm={phone_confirmed}), HeadDown={looking_down}, Hand={hand_interaction} -> Fast={fast_condition}, Only={phone_only_condition}, State={self.state}", flush=True)
               self._last_debug_time = now
               self._last_debug_state = self.state

        now = time.time()
        
        # 3. State Machine Transition
        if self.state == "OFF":
            # ---> Try to turn ON
            
            # (A) Fast ON
            if fast_condition or occlusion_condition:
                if self.fast_on_start is None: self.fast_on_start = now
                if now - self.fast_on_start >= self.fast_on_hold_sec:
                    self.state = "ON"
                    self._reset_timers()
            else:
                self.fast_on_start = None
                
            # (B) Phone Only ON (Fallback)
            if self.state == "OFF" and phone_only_condition:
                if self.phone_only_on_start is None: self.phone_only_on_start = now
                if now - self.phone_only_on_start >= self.phone_only_on_hold_sec:
                    self.state = "ON"
                    self._reset_timers()
            else:
                self.phone_only_on_start = None
                
        else: # State is ON
            # [NEW] Fast Focus (Recovery):
            # If no phone candidate signal, drop to OFF immediately.
            if not phone_candidate:
                 self.state = "OFF"
                 self._reset_timers()
                 return PhoneSignal(
                    phone_present=phone_candidate,
                    phone_conf=phone_conf,
                    phone_in_use_score=0.0,
                    phone_in_use=False
                 )

            # ---> Try to turn OFF
            # OFF Condition: Phone candidate gone OR (No hand interaction AND No looking down)
            # If even the weak candidate is gone, then it's definitely OFF.
            # Keep ON as long as we have any phone signal, even if hand/looking_down flickers.
            off_condition = (not phone_candidate) or (not (hand_interaction or looking_down or phone_confirmed))
            
            if off_condition:
                if self.off_start is None: self.off_start = now
                if now - self.off_start >= self.off_hold_sec:
                    self.state = "OFF"
                    self._reset_timers()
            else:
                self.off_start = None
        
        # 4. Result
        phone_in_use = (self.state == "ON")
        score = 1.0 if phone_in_use else 0.0

        return PhoneSignal(
            phone_present=phone_candidate,
            phone_conf=phone_conf,
            phone_in_use_score=score,
            phone_in_use=phone_in_use
        )

    def _reset_timers(self):
        self.fast_on_start = None
        self.phone_only_on_start = None
        self.off_start = None
