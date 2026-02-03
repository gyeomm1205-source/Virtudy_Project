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
        self.last_use_time = None
        
        # Configuration (Using centralized Config but logic from root)
        self.fast_on_hold_sec = 0.1
        self.phone_only_on_hold_sec = 0.05 # Faster detection for valid phone use
        self.off_hold_sec = 0.2    # Reduced from 0.5 to 0.2 for faster OFF transition
        # self.conf_th_phone_only removed (logic moved to process() with dual thresholds)

    def process(
        self,
        phone_conf: float,          
        is_cell_phone: bool,
        face_detected: bool,
        head_pitch: Optional[float],
        hand_interaction: bool,
        hand_near_face: bool = False,
        phone_near_face: bool = False,
        phone_area_ratio: float = 0.0
    ) -> PhoneSignal:
        
        # 1. Inputs
        # [NEW] Dual Threshold Logic
        # Candidate: Weakly detected (Needs corroboration like hand or head down)
        phone_candidate = is_cell_phone and phone_conf >= Config.PHONE_CANDIDATE_TH and phone_area_ratio >= Config.PHONE_BOX_AREA_TH
        
        # Confirmed: Strongly detected (Standalone)
        phone_confirmed = is_cell_phone and phone_conf >= Config.PHONE_CONFIRMED_TH and phone_area_ratio >= Config.PHONE_BOX_AREA_TH
        
        looking_down = (head_pitch is not None and head_pitch > Config.PITCH_PHONE_USE_TH)
        looking_down_hold = (head_pitch is not None and head_pitch > Config.PITCH_PHONE_HOLD_TH)
        # Use only direct hand-phone interaction as evidence.
        # Proxy signals (hand_near_face + phone_near_face) caused false ON holds.
        hand_present = hand_interaction
        phone_use_evidence = hand_present
        
        # 2. Logic Conditions
        # (A) Fast Condition: Candidate + (Hand or Head Down)
        # [Fix] Removed proxy logic (hand_near_face + looking_down) to prevent false positives
        # Now requires actual phone detection
        # Require hand evidence (or hand-near + looking-down) for actual "use"
        fast_condition = phone_candidate and phone_use_evidence
        
        # (B) Phone Only Condition: Confirmed Phone
        phone_only_condition = phone_confirmed
        
        now = time.time()
        if phone_use_evidence:
            self.last_use_time = now
        
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
            # Require behavior even for confirmed phone to avoid static-on-desk lock
            if self.state == "OFF" and phone_only_condition and phone_use_evidence:
                if self.phone_only_on_start is None: self.phone_only_on_start = now
                if now - self.phone_only_on_start >= self.phone_only_on_hold_sec:
                    self.state = "ON"
                    self._reset_timers()
            else:
                self.phone_only_on_start = None
                
        else: # State is ON
            # [NEW] Fast Focus (Recovery):
            # If I look UP (not looking down) AND there is no strong phone signal -> Immediate OFF
            # This fixes the "slow recovery" issue.
            if (not looking_down) and (not phone_confirmed):
                 self.state = "OFF"
                 self._reset_timers()
                 return PhoneSignal(
                    phone_present=phone_candidate,
                    phone_conf=phone_conf,
                    phone_in_use_score=0.0,
                    phone_in_use=False
                 )

            # ---> Try to turn OFF
            # OFF Condition: Phone candidate gone OR no hand evidence for a short while
            use_expired = (self.last_use_time is None) or ((now - self.last_use_time) > Config.PHONE_USE_RELEASE_SEC)
            off_condition = (not phone_candidate) or use_expired
            
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
            is_cell_phone=is_cell_phone,
            phone_conf=phone_conf,
            phone_in_use_score=score,
            phone_in_use=phone_in_use
        )

    def _reset_timers(self):
        self.fast_on_start = None
        self.phone_only_on_start = None
        self.off_start = None
        self.last_use_time = None
