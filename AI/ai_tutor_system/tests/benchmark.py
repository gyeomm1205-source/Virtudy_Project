import time
import os
import sys
import statistics
from datetime import datetime

# 상위 폴더의 모듈을 불러오기 위한 경로 설정
sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))

from core.database import init_db
from mock.mock_generator import generate_mock_week
from services.analyzer import calculate_stats
from services.feedback_tutor import get_weekly_report

def run_performance_test(iterations=3):
    print(f"🚀 [성능 벤치마크] {iterations}회 반복 테스트를 시작합니다. (대상 모델: Gemma 2)")
    
    total_latencies = []
    ai_latencies = []
    accuracies = []
    
    # 1. DB 초기화
    init_db()

    for i in range(iterations):
        print(f"\n--- {i+1}회차 테스트 진행 중 ---")
        
        # [Step 1] 데이터 생성 (합성 데이터 7일치)
        generate_mock_week()
        
        # [Step 2] 분석 및 리포트 생성 시간 측정
        start_time = time.time()
        
        # 데이터 분석 (순공 시간 계산)
        stats = calculate_stats()
        analysis_end_time = time.time()
        
        # Gemma 2 리포트 생성
        report = get_weekly_report(stats)
        ai_end_time = time.time()
        
        # [데이터 기록]
        iter_total_latency = ai_end_time - start_time
        iter_ai_latency = ai_end_time - analysis_end_time
        total_latencies.append(iter_total_latency)
        ai_latencies.append(iter_ai_latency)
        
        # [정확도 및 지시 이행 검증]
        is_korean = any('가' <= char <= '힣' for char in report)
        # 자소서용 지표: 리포트 내 필수 키워드 포함 여부 (진단, 기회비용, 목표, 등급)
        essential_keywords = ["진단", "기회비용", "목표", "등급"]
        keyword_score = sum(1 for k in essential_keywords if k in report) / len(essential_keywords) * 100
        accuracies.append(keyword_score)

        print(f"✅ 완료: 전체 {iter_total_latency:.2f}s (AI 추론: {iter_ai_latency:.2f}s)")
        print(f"📊 한국어 여부: {'성공' if is_korean else '실패'} | 지시 이행도: {keyword_score}%")

    # --- 최종 결과 요약 ---
    print("\n" + "="*60)
    print("📈 최종 성능 벤치마크 리포트 (자소서 활용 지표)")
    print(f"- 평균 전체 소요 시간: {statistics.mean(total_latencies):.2f}초")
    print(f"- 평균 AI 추론 속도: {statistics.mean(ai_latencies):.2f}초")
    print(f"- 지시 이행률 (Instruction Adherence): {statistics.mean(accuracies):.1f}%")
    print(f"- 테스트 데이터 규모: 일주일치 로그 ({stats['total']}건)")
    print("-" * 60)
    print("💡 Tip: 이 수치를 개발 위키와 자소서의 '기술적 성과' 항목에 활용하세요.")
    print("="*60)

if __name__ == "__main__":
    run_performance_test()