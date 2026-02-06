import os

# core/config.py
class Config:
    # --- Absence ---
<<<<<<< HEAD
    ABSENT_WINDOW = 15          # Frame window size
    ABSENT_TRUE_RATIO = 0.8     # Ratio of frames to confirm absence
    ABSENT_GRACE_SEC = 3.0      # [TUNED] Grace period before absence triggers
=======
    ABSENT_WINDOW = 20          # Heavy smoothing for 180p noise
    ABSENT_TRUE_RATIO = 0.8     
>>>>>>> 1d12e087b06e8ccc4de00953fd963920a5f14c00

    # --- Drowsy ---
    EAR_DROWSY_TH = 0.18        # Threshold for Eye Aspect Ratio (Adjusted for user)
    EAR_AWAKE_TH = 0.26         # [TUNED] Fast Awake Threshold (Lowered 0.30 -> 0.26 for easier wake-up)
    PITCH_DROWSY_TH = 0.45      # Threshold for Head Pitch (Ratio, not degrees)
    DROWSY_WINDOW = 10          # [TUNED] Reduced window size (13 -> 10) for faster reaction
    DROWSY_TRUE_RATIO = 0.8     # High ratio -> Hard to enter sleep (blink safe), Fast to exit (wake up)

    # --- Phone ---
    PHONE_CANDIDATE_TH = 0.17   # [TUNED] Slightly higher to reduce false positives
    PHONE_CONFIRMED_TH = 0.28   # [TUNED] Slightly higher to require stronger phone signal
    PHONE_USE_WINDOW = 15
    PHONE_USE_TRUE_RATIO = 0.7
    PHONE_USE_RELEASE_SEC = 0.2  # Grace period before releasing PHONE when evidence disappears
    YOLO_EVERY_N_FRAMES = 5     # Fallback when interval is 0 (unused by default)
    YOLO_INTERVAL_SEC = 0.3     # Run YOLO by time interval (seconds)
    YOLO_IMG_SIZE = 640         # Resize input for YOLO only (tradeoff: speed vs accuracy)
    FACE_EVERY_N_FRAMES = 2     # Run FaceMesh every N frames (set to 1 for reliable drowsy)
    HAND_EVERY_N_FRAMES = 5     # Run Hands every N frames (if not phone-only)
    HANDS_BURST_SEC = 0.5       # Force Hands for a short window after phone detection
    HANDS_ONLY_ON_PHONE = True  # Run Hands only during burst window
    ENABLE_HANDS = True         # Enable hand landmarks
    PROCESSING_MAX_SIZE = 640   # Downscale input for processing (max side), 0 disables
    PHONE_CACHE_MAX_AGE = 3     # Clear cached phone detection after N frames
    PHONE_CACHE_TTL_SEC = 1.0   # Max age in seconds to keep last phone box
    HAND_CACHE_MAX_AGE = 6      # Clear cached hand detection after N frames
    DROWSY_MIN_CLOSED_FRAMES = 4  # Min consecutive closed-eye frames to avoid blink
    DROWSY_MIN_CLOSED_SEC = 0.4   # Min closed-eye duration to avoid blink
    DROWSY_TRIGGER_SCORE = 0.6    # Candidate-level trigger (with blink filter above)
    
    # Head down threshold for phone usage (can be more sensitive than drowsy)
    PITCH_PHONE_USE_TH = 0.37   # [TUNED] On threshold
    PITCH_PHONE_RELEASE_TH = 0.34  # [NEW] Lower threshold to reduce ON/OFF flapping
    PITCH_PHONE_HOLD_TH = 0.44  # Require stronger head-down to keep PHONE state
    PHONE_NEAR_FACE_RATIO = 0.18  # Phone center within this ratio of face center (diag)
    PHONE_BOX_AREA_TH = 0.020     # Phone box area ratio (w*h) threshold for "in use"
    PHONE_BOX_AREA_TH_HAND = 0.015 # Lower area threshold when hand interaction is present
    PHONE_CONF_FOR_USE = 0.70     # Minimum conf for phone usage evidence
    PHONE_NEAR_FACE_Y_OFFSET = 0.08  # Require phone center below face center by this ratio
    PHONE_DISTRACTOR_CLASSES = [39, 40, 41]  # bottle, wine glass, cup
    PHONE_DISTRACTOR_CONF_TH = 0.35          # Suppress phone if distractor is strong
    PHONE_DISTRACTOR_MARGIN = 0.15           # Phone must exceed distractor by this margin
    PHONE_DISTRACTOR_IOU_TH = 0.10           # Suppress only when boxes overlap
    # --- Scoring ---
    # Penalty/Reward per second
    SCORE_RATE_SCALE = 5.0     # [NEW] Scale score change rate (1x -> 5x)
    REWARD_FOCUSED = 1.0        # [NEW] Reward for focusing
    PENALTY_FOCUSED = 0.0
    PENALTY_DROWSY = 1.0        # Match reward rate (1 point/sec)
    PENALTY_PHONE = 1.0         
    PENALTY_ABSENT = 1.0        
    PENALTY_UNKNOWN = 0.0

    # --- Kafka ---
<<<<<<< HEAD
    # KAFKA_BOOTSTRAP_SERVERS = ['localhost:9092'] # Localhost execution
    KAFKA_BOOTSTRAP_SERVERS = [os.getenv('KAFKA_BOOTSTRAP_SERVERS', 'localhost:9092')]
=======
    
    # [Local 실행 시]
    KAFKA_BOOTSTRAP_SERVERS = ['localhost:9092']
    # [Docker 배포 시 - 이걸로 변경 필요!]
    # KAFKA_BOOTSTRAP_SERVERS = ['kafka:29092']
>>>>>>> 1d12e087b06e8ccc4de00953fd963920a5f14c00
    KAFKA_TOPIC = 'study-log-topic'
    LOG_COOLDOWN = 60.0  # Seconds

