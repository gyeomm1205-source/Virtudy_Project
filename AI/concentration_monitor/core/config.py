# core/config.py
class Config:
    # --- Absence ---
    ABSENT_WINDOW = 15          # Frame window size
    ABSENT_TRUE_RATIO = 0.8     # Ratio of frames to confirm absence

    # --- Drowsy ---
    EAR_DROWSY_TH = 0.23        # Threshold for Eye Aspect Ratio (Adjusted for user)
    EAR_AWAKE_TH = 0.30         # [NEW] Fast Awake Threshold (Lowered 0.35 -> 0.30 for faster recovery)
    PITCH_DROWSY_TH = 0.45      # Threshold for Head Pitch (Ratio, not degrees)
    DROWSY_WINDOW = 13          # User requested size (Robust against blinks)
    DROWSY_TRUE_RATIO = 0.8     # High ratio -> Hard to enter sleep (blink safe), Fast to exit (wake up)

    # --- Phone ---
    PHONE_CANDIDATE_TH = 0.20   # [NEW] Moderate candidate (0.15 was too sensitive to noise)
    PHONE_CONFIRMED_TH = 0.30   # [NEW] Easier confirmation
    PHONE_USE_WINDOW = 15
    PHONE_USE_TRUE_RATIO = 0.7
    
    # Head down threshold for phone usage (can be more sensitive than drowsy)
    PITCH_PHONE_USE_TH = 0.35   # [NEW] Reverted to 0.35 (0.40 was too strict)   

    # --- Scoring ---
    # Penalty/Reward per second
    REWARD_FOCUSED = 1.0        # [NEW] Reward for focusing
    PENALTY_FOCUSED = 0.0
    PENALTY_DROWSY = 3.0
    PENALTY_PHONE = 2.0
    PENALTY_ABSENT = 5.0
    PENALTY_UNKNOWN = 0.5

    # --- Kafka ---
    KAFKA_BOOTSTRAP_SERVERS = ['localhost:9092'] # Localhost execution
    KAFKA_TOPIC = 'study-log-topic'
    LOG_COOLDOWN = 60.0  # Seconds

