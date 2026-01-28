import pandas as pd
import sqlite3
import os

DB_PATH = "data/study_logs.db"

def calculate_stats():
    if not os.path.exists(DB_PATH): return None
    
    conn = sqlite3.connect(DB_PATH)
    df = pd.read_sql_query("SELECT * FROM study_logs", conn)
    conn.close()

    if df.empty: return None

    # 전체 로그 개수를 분 단위 시간으로 간주 (1분 1로그 가정)
    total_min = len(df)
    # 방해 요소(SLEEP, PHONE, AWAY) 시간 합산
    distraction_df = df[df['event_type'].isin(['SLEEP', 'PHONE', 'AWAY'])]
    distraction_min = len(distraction_df)
    
    pure_study_min = total_min - distraction_min
    focus_rate = (pure_study_min / total_min * 100) if total_min > 0 else 0

    return {
        "total": total_min,
        "pure": pure_study_min,
        "rate": round(focus_rate, 1),
        "top_issue": distraction_df['event_type'].mode()[0] if not distraction_df.empty else "없음"
    }