import sqlite3
import random
import os
from datetime import datetime, timedelta

# 상위 폴더의 data 디렉토리를 가리킴
DB_PATH = os.path.join(os.path.dirname(__file__), "../data/study_logs.db")

def generate_mock_week():
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    cursor.execute("DELETE FROM study_logs") # 기존 데이터 초기화
    
    start_time = datetime.now() - timedelta(days=7)
    for day in range(7):
        current = start_time + timedelta(days=day, hours=9)
        for _ in range(300): # 매일 5시간 분량
            event = "FOCUS" if random.random() > 0.3 else random.choice(["PHONE", "SLEEP", "AWAY"])
            cursor.execute("INSERT INTO study_logs (session_id, event_type, detected_at) VALUES (?, ?, ?)",
                           (f"test-session-{day}", event, current.isoformat()))
            current += timedelta(minutes=1)
            
    conn.commit()
    conn.close()
    print(f"✅ 테스트 데이터가 생성되었습니다: {DB_PATH}")

if __name__ == "__main__":
    generate_mock_week()