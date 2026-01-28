from core.database import init_db
from mock.mock_generator import generate_mock_week
from services.analyzer import calculate_stats
from services.feedback_tutor import get_weekly_report

def main():
    init_db()               # 1. DB 초기화
    generate_mock_week()     # 2. 테스트 데이터 생성
    
    print("📊 데이터 분석 중...")
    stats = calculate_stats() # 3. 통계 계산
    
    if stats:
        print("🤖 Gemma 2 리포트 생성 중...")
        report = get_weekly_report(stats) # 4. AI 리포트 생성
        print("\n" + "="*50 + "\n" + report + "\n" + "="*50)

if __name__ == "__main__":
    main()