# core/config.py
class Config:
    # --- Absence ---
    ABSENT_WINDOW = 15          # Frame window size
    ABSENT_TRUE_RATIO = 0.8     # Ratio of frames to confirm absence

    # --- Drowsy ---
    EAR_DROWSY_TH = 0.20        # Threshold for Eye Aspect Ratio (closed eyes)
    PITCH_DROWSY_TH = 15.0      # Threshold for Head Pitch (looking down considerably)
    DROWSY_WINDOW = 15
    DROWSY_TRUE_RATIO = 0.7

    # --- Phone ---
    PHONE_PRESENT_TH = 0.40     # YOLO confidence threshold
    PHONE_USE_WINDOW = 15
    PHONE_USE_TRUE_RATIO = 0.7
    
    # Head down threshold for phone usage (can be more sensitive than drowsy)
    PITCH_PHONE_USE_TH = 10.0   

    # --- Scoring ---
    # Penalty per second
    PENALTY_FOCUSED = 0.0
    PENALTY_DROWSY = 3.0
    PENALTY_PHONE = 2.0
    PENALTY_ABSENT = 5.0
    PENALTY_UNKNOWN = 0.5
