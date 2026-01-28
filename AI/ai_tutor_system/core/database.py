import sqlite3
import os

# DB 파일이 저장될 경로 설정
DB_PATH = os.path.join(os.path.dirname(__file__), "../data/study_logs.db")

def init_db():
    os.makedirs(os.path.dirname(DB_PATH), exist_ok=True)
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    # 백엔드 DTO 규격을 반영한 테이블 생성
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS study_logs (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            session_id TEXT,
            event_type TEXT,
            detected_at TIMESTAMP
        )
    ''')
    conn.commit()
    conn.close()
    print("✅ 로컬 데이터베이스가 준비되었습니다.")