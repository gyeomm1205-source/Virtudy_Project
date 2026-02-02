# core/config.py
class Config:
    # --- Absence ---
    ABSENT_WINDOW = 20          # Heavy smoothing for 180p noise
    ABSENT_TRUE_RATIO = 0.8     

    # --- Drowsy ---
<<<<<<< HEAD
    EAR_DROWSY_TH = 0.23        
    EAR_AWAKE_TH = 0.30         
    PITCH_DROWSY_TH = 0.40      
    DROWSY_WINDOW = 15          # Increased from 4 to 15 to combat 180p flickering
    DROWSY_TRUE_RATIO = 0.8     

    # --- Phone ---
    PHONE_CANDIDATE_TH = 0.15   
    PHONE_CONFIRMED_TH = 0.25   
    PHONE_USE_WINDOW = 20       # Heavy smoothing to prevent flickering
    PHONE_USE_TRUE_RATIO = 0.7
    
    # Head down threshold
    PITCH_PHONE_USE_TH = 0.35   

    # --- Scoring ---
    # Penalty/Reward per second
    REWARD_FOCUSED = 2.0        # Recover score faster
=======
    EAR_DROWSY_TH = 0.18        # Threshold for Eye Aspect Ratio (Adjusted for user)
    EAR_AWAKE_TH = 0.26         # [TUNED] Fast Awake Threshold (Lowered 0.30 -> 0.26 for easier wake-up)
    PITCH_DROWSY_TH = 0.45      # Threshold for Head Pitch (Ratio, not degrees)
    DROWSY_WINDOW = 10          # [TUNED] Reduced window size (13 -> 10) for faster reaction
    DROWSY_TRUE_RATIO = 0.8     # High ratio -> Hard to enter sleep (blink safe), Fast to exit (wake up)

    # --- Phone ---
    PHONE_CANDIDATE_TH = 0.10   # [TUNED] Lowered (0.20 -> 0.10) to detect phone more easily
    PHONE_CONFIRMED_TH = 0.30   # [NEW] Easier confirmation
    PHONE_USE_WINDOW = 15
    PHONE_USE_TRUE_RATIO = 0.7
    
    # Head down threshold for phone usage (can be more sensitive than drowsy)
    PITCH_PHONE_USE_TH = 0.40   # [NEW] Reverted to 0.40 (Optimized)   

    # --- Scoring ---
    # Penalty/Reward per second
    REWARD_FOCUSED = 1.0        # [NEW] Reward for focusing
>>>>>>> origin/ai
    PENALTY_FOCUSED = 0.0
    PENALTY_DROWSY = 4.0        # Stricter penalty
    PENALTY_PHONE = 4.0         
    PENALTY_ABSENT = 6.0        
    PENALTY_UNKNOWN = 0.5

    # --- Kafka ---
<<<<<<< HEAD
    
    # [Local 실행 시]
    KAFKA_BOOTSTRAP_SERVERS = ['localhost:9092']
    # [Docker 배포 시 - 이걸로 변경 필요!]
    # KAFKA_BOOTSTRAP_SERVERS = ['kafka:29092']
=======
    KAFKA_BOOTSTRAP_SERVERS = ['localhost:9092'] # Localhost execution
>>>>>>> origin/ai
    KAFKA_TOPIC = 'study-log-topic'
    LOG_COOLDOWN = 60.0  # Seconds

