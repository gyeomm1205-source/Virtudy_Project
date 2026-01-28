import sqlite3

conn = sqlite3.connect('data/study_logs.db')
cursor = conn.cursor()

# 최근 10개의 로그 출력
cursor.execute("SELECT * FROM study_logs ORDER BY detected_at DESC LIMIT 10")
rows = cursor.fetchall()

print("📋 최근 수집된 실시간 로그:")
for row in rows:
    print(row)

conn.close()