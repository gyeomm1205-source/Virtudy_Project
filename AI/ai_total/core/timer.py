# core/timer.py
import time

class IntervalTimer:
    def __init__(self, interval: float):
        self.interval = interval
        self.last_time = time.time()

    def check(self) -> bool:
        now = time.time()
        if now - self.last_time >= self.interval:
            self.last_time = now
            return True
        return False
