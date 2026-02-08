import argparse
import datetime
import uuid
import json
import time
import random
from kafka import KafkaProducer

# Configuration
KAFKA_BOOTSTRAP_SERVERS = ['localhost:9092']
TOPIC_NAME = 'study-log-topic'
KST_TIMEZONE_OFFSET = datetime.timedelta(hours=9)

def get_current_kst():
    return datetime.datetime.utcnow() + KST_TIMEZONE_OFFSET

def generate_historical_sql(member_id, days):
    """
    과거 N일치 데이터를 생성하여 SQL 파일로 저장 (FK 문제 해결 버전)
    """
    filename = "insert_mock_history.sql"
    end_date = get_current_kst().date()
    start_date = end_date - datetime.timedelta(days=days)

    print(f"[Mode: Historical] Generating SQL for period: {start_date} ~ {end_date}")

    with open(filename, "w", encoding="utf-8") as f:
        f.write("-- Mock Data Insertion Script\n")
        f.write("-- IMPORTANT: Ensure the member_id exists in 'member' table\n\n")

        # 1. 룸 ID 확보
        f.write(f"-- 1. Get Room PK\n")
        f.write(f"SET @member_pk = (SELECT id FROM member WHERE member_id = '{member_id}');\n")

        # 룸이 없으면 가장 최근 방을 가져옴
        f.write("SET @room_pk = (SELECT id FROM study_room WHERE member_id = @member_pk ORDER BY created_at DESC LIMIT 1);\n")
        f.write("-- If @room_pk is NULL, create a room manually first.\n\n")

        current_date = start_date
        while current_date <= end_date:
            session_uuid = str(uuid.uuid4())
            # 하루에 1개 세션, 오전 9시 ~ 11시 (2시간)
            start_dt = datetime.datetime.combine(current_date, datetime.time(9, 0, 0))
            end_dt = datetime.datetime.combine(current_date, datetime.time(11, 0, 0))

            # 실제 공부 시간 (초 단위, 랜덤)
            real_study_time = random.randint(3000, 7000)

            # 2. Session Insert
            f.write(f"-- Date: {current_date}\n")
            sql_session = f"""
INSERT INTO study_session (session_id, start_time, end_time, session_real_study_time, created_at, last_modfied_at, MEMBER_ID, ROOM_ID)
VALUES ('{session_uuid}', '{start_dt}', '{end_dt}', {real_study_time}, NOW(), NOW(), @member_pk, @room_pk);
"""
            f.write(sql_session)

            # 3. Capture Session PK (핵심: FK 연결을 위해 변수에 저장)
            f.write("SET @last_session_pk = LAST_INSERT_ID();\n")

            # 4. Logs Insert (FK로 @last_session_pk 사용)
            logs = [
                ('FOCUS', start_dt),
                ('SLEEP', start_dt + datetime.timedelta(minutes=30)),
                ('FOCUS', start_dt + datetime.timedelta(minutes=40)),
                ('AWAY', start_dt + datetime.timedelta(minutes=90)),
                ('FOCUS', start_dt + datetime.timedelta(minutes=100))
            ]

            for event, log_time in logs:
                log_uuid = str(uuid.uuid4())
                sql_log = f"""
INSERT INTO study_log (log_id, event_type, detected_at, member_id, session_id)
VALUES ('{log_uuid}', '{event}', '{log_time}', @member_pk, @last_session_pk);
"""
                f.write(sql_log)

            f.write("\n")
            current_date += datetime.timedelta(days=1)

    print(f"[Success] SQL file created: {filename}")
    print("Execute this file in your MySQL Workbench or via docker exec.")


def run_realtime_simulation(member_id, session_id=None):
    """
    실시간으로 현재 시간 기준 로그를 Kafka로 전송
    """
    producer = KafkaProducer(
        bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
        value_serializer=lambda v: json.dumps(v).encode('utf-8')
    )

    print(f"[Mode: Realtime] Sending logs to Kafka for member: {member_id}")
    print("Press Ctrl+C to stop.")

    # 사용자가 입력한 세션 ID가 있으면 사용, 없으면 랜덤 생성 (DB에 없으면 에러 발생 가능)
    target_session_id = session_id if session_id else str(uuid.uuid4())
    print(f"Target Session ID: {target_session_id}")

    try:
        while True:
            # 현재 시간 (KST)
            now = datetime.datetime.utcnow() + datetime.timedelta(hours=9)
            now_str = now.isoformat()

            # 랜덤 이벤트 발생 (주로 FOCUS)
            rand_val = random.random()
            if rand_val < 0.8:
                event_type = 'FOCUS'
            elif rand_val < 0.9:
                event_type = 'PHONE'
            else:
                event_type = 'SLEEP'

            log_data = {
                'sessionId': target_session_id,
                'memberId': member_id,
                'eventType': event_type,
                'detectedAt': now_str
            }

            producer.send(TOPIC_NAME, log_data)
            print(f"[Sent] {event_type} at {now_str}")

            # 1분 스케줄링 테스트를 위해 10초마다 전송
            time.sleep(10)

    except KeyboardInterrupt:
        producer.flush()
        print("\n[Stopped] Realtime simulation ended.")

def main():
    parser = argparse.ArgumentParser(description='Study Mock Data Generator')
    parser.add_argument('--memberId', required=True, help='Target Member ID (String)')
    parser.add_argument('--mode', choices=['historical', 'realtime'], required=True, help='Execution Mode')
    parser.add_argument('--days', type=int, default=14, help='Days to generate for historical mode')
    parser.add_argument('--server', default='localhost:9092', help='Kafka Bootstrap Server')
    parser.add_argument('--sessionId', help='Specific Session ID for realtime mode (Must exist in DB)')

    args = parser.parse_args()

    global KAFKA_BOOTSTRAP_SERVERS
    KAFKA_BOOTSTRAP_SERVERS = [args.server]

    if args.mode == 'historical':
        generate_historical_sql(args.memberId, args.days)
    elif args.mode == 'realtime':
        run_realtime_simulation(args.memberId, args.sessionId)

if __name__ == "__main__":
    main()