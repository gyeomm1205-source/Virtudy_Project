# core/config.py
class Config:
    # --- Absence ---
    ABSENT_WINDOW = 20          # Heavy smoothing for 180p noise
    ABSENT_TRUE_RATIO = 0.8     

    # --- Drowsy ---
    EAR_DROWSY_TH = 0.18        # Threshold for Eye Aspect Ratio (Adjusted for user)
    EAR_AWAKE_TH = 0.26         # [TUNED] Fast Awake Threshold (Lowered 0.30 -> 0.26 for easier wake-up)
    PITCH_DROWSY_TH = 0.45      # Threshold for Head Pitch (Ratio, not degrees)
    DROWSY_WINDOW = 10          # [TUNED] Reduced window size (13 -> 10) for faster reaction
    DROWSY_TRUE_RATIO = 0.8     # High ratio -> Hard to enter sleep (blink safe), Fast to exit (wake up)

    # --- Phone ---
    PHONE_CANDIDATE_TH = 0.10   # [TUNED] Lowered (0.20 -> 0.10) to detect phone more easily
    PHONE_CONFIRMED_TH = 0.20   # [TUNED] Lowered (0.30 -> 0.20) for easier standalone detection
    PHONE_USE_WINDOW = 15
    PHONE_USE_TRUE_RATIO = 0.7
    PHONE_USE_RELEASE_SEC = 0.2  # Grace period before releasing PHONE when evidence disappears
    YOLO_EVERY_N_FRAMES = 3     # Run YOLO every N frames to boost FPS
    YOLO_IMG_SIZE = 640         # Resize input for YOLO only (tradeoff: speed vs accuracy)
    FACE_EVERY_N_FRAMES = 1     # Run FaceMesh every N frames (set to 1 for reliable drowsy)
    HAND_EVERY_N_FRAMES = 3     # Run Hands every N frames
    PHONE_CACHE_MAX_AGE = 3     # Clear cached phone detection after N frames
    HAND_CACHE_MAX_AGE = 6      # Clear cached hand detection after N frames
    DROWSY_MIN_CLOSED_FRAMES = 4  # Min consecutive closed-eye frames to avoid blink
    DROWSY_MIN_CLOSED_SEC = 0.4   # Min closed-eye duration to avoid blink
    DROWSY_TRIGGER_SCORE = 0.6    # Candidate-level trigger (with blink filter above)
    
    # Head down threshold for phone usage (can be more sensitive than drowsy)
    PITCH_PHONE_USE_TH = 0.35   # [TUNED] Lowered (0.40 -> 0.35) for more sensitive head-down detection
    PITCH_PHONE_HOLD_TH = 0.42  # Require stronger head-down to keep PHONE state
    PHONE_NEAR_FACE_RATIO = 0.18  # Phone center within this ratio of face center (diag)
    PHONE_BOX_AREA_TH = 0.020     # Phone box area ratio (w*h) threshold for "in use"
    PHONE_CONF_FOR_USE = 0.60     # Minimum conf for phone usage evidence
    PHONE_NEAR_FACE_Y_OFFSET = 0.08  # Require phone center below face center by this ratio

    # --- Scoring ---
    # Penalty/Reward per second
    REWARD_FOCUSED = 1.0        # [NEW] Reward for focusing
    PENALTY_FOCUSED = 0.0
    PENALTY_DROWSY = 4.0        # Stricter penalty
    PENALTY_PHONE = 4.0         
    PENALTY_ABSENT = 6.0        
    PENALTY_UNKNOWN = 0.5

    # --- Kafka ---
    
    # [Local 실행 시]
    KAFKA_BOOTSTRAP_SERVERS = ['localhost:9092']
    # [Docker 배포 시 - 이걸로 변경 필요!]
    # KAFKA_BOOTSTRAP_SERVERS = ['kafka:29092']
    KAFKA_TOPIC = 'study-log-topic'
    LOG_COOLDOWN = 60.0  # Seconds

