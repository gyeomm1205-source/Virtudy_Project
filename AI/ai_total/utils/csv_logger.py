# utils/csv_logger.py
import csv
import time
import os
from datetime import datetime

class CSVLogger:
    def __init__(self, log_dir="logs"):
        if not os.path.exists(log_dir):
            os.makedirs(log_dir)
            
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        self.filename = os.path.join(log_dir, f"monitor_log_{timestamp}.csv")
        
        # Define Header
        self.fieldnames = [
            'timestamp', 
            'elapsed_sec',
            'state', 
            'score', 
            'ear', 
            'pitch', 
            'phone_conf', 
            'hand_interaction', 
            'raw_absent', 
            'drowsy_score', 
            'phone_in_use'
        ]
        
        try:
            self.file = open(self.filename, mode='w', newline='', encoding='utf-8')
            self.writer = csv.DictWriter(self.file, fieldnames=self.fieldnames)
            self.writer.writeheader()
            print(f"[INFO] Logging to {self.filename}")
        except Exception as e:
            print(f"[ERROR] Failed to open log file: {e}")
            self.file = None
            self.writer = None
            
        self.start_time = time.time()

    def log(self, data: dict):
        if not self.writer:
            return
            
        # Add timestamp/elapsed if not in data
        if 'timestamp' not in data:
            data['timestamp'] = datetime.now().strftime("%H:%M:%S.%f")[:-3]
        if 'elapsed_sec' not in data:
            data['elapsed_sec'] = round(time.time() - self.start_time, 3)
            
        try:
            self.writer.writerow(data)
            # Flush periodically? Maybe not every frame for performance, 
            # but for safety let's flush every time or use unbuffered.
            # Python standard buffering is usually fine.
            self.file.flush() 
        except Exception as e:
            # print(f"[WARN] Log write failed: {e}")
            pass

    def close(self):
        if self.file:
            self.file.close()
            self.file = None
