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
        
        # Configuration (Using centralized Config but logic from root)
        self.fast_on_hold_sec = 0.2
        self.phone_only_on_hold_sec = 0.3
        self.off_hold_sec = 0.5    # Reduced from 1.5 to 0.5 for faster OFF transition
        self.conf_th_phone_only = 0.15 # Corresponds to Config.PHONE_PRESENT_TH

    def process(
        self,
        phone_conf: float,          
        face_detected: bool,
        head_pitch: Optional[float],
        hand_interaction: bool     
    ) -> PhoneSignal:
        
        # 1. Inputs
        phone_present = phone_conf >= Config.PHONE_PRESENT_TH
        looking_down = (head_pitch is not None and head_pitch > Config.PITCH_PHONE_USE_TH)
        
        # 2. Logic Conditions
        # (A) Fast Condition: Phone + (Hand or Head Down)
        # Note: In root file 'hand_near' was calculated by distance. 
        # Here 'hand_interaction' is passed in (calculated in run_livekit via run.FeatureExtractor)
        fast_condition = phone_present and (hand_interaction or looking_down)
        
        # (B) Phone Only Condition: Phone + High Confidence
        phone_only_condition = phone_present and (phone_conf >= self.conf_th_phone_only)
        
        now = time.time()
        
        # 3. State Machine Transition
        if self.state == "OFF":
            # ---> Try to turn ON
            
            # (A) Fast ON
            if fast_condition:
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
            # ---> Try to turn OFF
            # OFF Condition: Phone gone OR (No hand interaction AND No looking down)
            # Root logic: "off_condition = (not phone_present) or (not (hand_near or head_down))"
            # This means if I just look up but phone is still there -> OFF? 
            # Actually root logic says: if phone is gone OR behavior is gone -> Start counting OFF.
            
            off_condition = (not phone_present) or (not (hand_interaction or looking_down))
            
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
            phone_present=phone_present,
            phone_conf=phone_conf,
            phone_in_use_score=score,
            phone_in_use=phone_in_use
        )

    def _reset_timers(self):
        self.fast_on_start = None
        self.phone_only_on_start = None
        self.off_start = None
