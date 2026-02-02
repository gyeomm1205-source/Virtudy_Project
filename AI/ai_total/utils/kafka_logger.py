import json
import time
from datetime import datetime
from kafka import KafkaProducer
from core.config import Config
from core.types import FocusState

class KafkaLogger:
    def __init__(self, session_id: str):
        self.session_id = session_id
        self.topic = Config.KAFKA_TOPIC
        self.cooldown = Config.LOG_COOLDOWN
        self.last_sent_time = 0
        self.last_event_type = "FOCUS"
        self.producer = None
        
        # Mapping internally used FocusState to Backend Protocol Strings
        self.state_map = {
            FocusState.FOCUSED: "FOCUS",
            FocusState.DROWSY: "SLEEP",
            FocusState.PHONE: "PHONE",
            FocusState.ABSENT: "AWAY",
            FocusState.UNKNOWN: "FOCUS"
        }

        try:
            print(f"[INFO] Connecting to Kafka at {Config.KAFKA_BOOTSTRAP_SERVERS}...", flush=True)
            self.producer = KafkaProducer(
                bootstrap_servers=Config.KAFKA_BOOTSTRAP_SERVERS,
                value_serializer=lambda x: json.dumps(x).encode('utf-8'),
                key_serializer=lambda x: x.encode('utf-8'),
                # Short timeout for initialization to avoid blocking for too long if Kafka is down
                request_timeout_ms=2000,
                api_version_auto_timeout_ms=2000
            )
            print("[INFO] Kafka Producer initialized.", flush=True)
        except Exception as e:
            print(f"[WARN] Failed to initialize Kafka Producer: {e}", flush=True)
            self.producer = None

    def log_state(self, current_state: FocusState):
        if not self.producer:
            return

        current_event = self.state_map.get(current_state, "FOCUS")
        current_time = time.time()
        should_send = False

        # (A) State Changed
        if current_event != self.last_event_type:
            should_send = True
        
        # (B) State Persisted (Heartbeat for non-FOCUS states)
        elif current_event != "FOCUS" and (current_time - self.last_sent_time > self.cooldown):
            should_send = True
            
        if should_send:
            self._send(current_event)
            self.last_event_type = current_event
            self.last_sent_time = current_time

    def _send(self, event_type):
        try:
            payload = {
                "sessionId": self.session_id,
                "eventType": event_type,
                "detectedAt": datetime.now().isoformat()
            }
            # send() is asynchronous
            self.producer.send(self.topic, key=self.session_id, value=payload)
            print(f"[KAFKA] Sent: {event_type} at {payload['detectedAt']}", flush=True)
        except Exception as e:
            print(f"[ERROR] Kafka send failed: {e}", flush=True)

    def close(self):
        if self.producer:
            try:
                self.producer.flush()
                self.producer.close()
                print("[INFO] Kafka Producer closed.", flush=True)
            except Exception as e:
                print(f"[ERROR] Error closing Kafka producer: {e}", flush=True)
