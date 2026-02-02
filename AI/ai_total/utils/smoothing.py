# utils/smoothing.py
from collections import deque

class BoolWindow:
    def __init__(self, size: int, true_ratio: float):
        self.size = size
        self.true_ratio = true_ratio
        self.q = deque(maxlen=size)

    def update(self, value: bool) -> bool:
        self.q.append(bool(value))
        if len(self.q) < self.size:
            # Not enough data yet, return False to be conservative, 
            # or return based on current partial content? 
            # Let's be conservative.
            return False
            
        trues = sum(1 for v in self.q if v)
        return (trues / self.size) >= self.true_ratio
