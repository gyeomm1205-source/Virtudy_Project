// src/features/report/logic/useWeeklyReport.ts
import { ref, computed, onMounted } from 'vue';
import { getWeeklyReport } from '../api/reportApi';
import type { ReportResponse } from '../types/report.types';

export const useWeeklyReport = () => {
  // 상태
  const isLoading = ref(false);
  const reportData = ref<ReportResponse | null>(null);
  
  // 현재 기준 날짜 (기본값: 오늘)
  const baseDate = ref(new Date());

  // 날짜 유틸리티: 해당 날짜가 속한 주의 월요일과 일요일 구하기
  const getWeekRange = (date: Date) => {
    const current = new Date(date);
    // 시간 정보 초기화 (날짜 계산 꼬임 방지)
    current.setHours(0, 0, 0, 0);

    const day = current.getDay(); // 0(일) ~ 6(토)
    // 월요일 계산 로직: 
    // 일요일(0)이면 -6일, 그 외에는 (오늘 - 요일 + 1)
    const diffToMon = current.getDate() - day + (day === 0 ? -6 : 1); 
    
    const monday = new Date(current);
    monday.setDate(diffToMon);
    
    const sunday = new Date(monday);
    sunday.setDate(monday.getDate() + 6);
    
    return { monday, sunday };
  };

  // 현재 선택된 주의 시작/끝 날짜 (Date 객체)
  const currentWeek = computed(() => getWeekRange(baseDate.value));

  // [수정] 백엔드 전송용 문자열 (YYYY-MM-DD)
  // toISOString()은 UTC 기준이라 한국 시간 00시가 전날로 찍히는 문제 해결
  const formatDate = (date: Date): string => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0'); // 월은 0부터 시작하므로 +1
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  // 화면 표시용 (YYYY Month)
  const displayMonth = computed(() => {
    const m = baseDate.value.getMonth() + 1;
    const y = baseDate.value.getFullYear();
    // 영문 월 표시가 필요하면 여기서 변환 (예: July 2023)
    const monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
    return `${monthNames[m-1]} ${y}`;
  });

  // 데이터 가져오기
  const fetchReport = async () => {
    isLoading.value = true;
    try {
      const { monday, sunday } = currentWeek.value;
      const data = await getWeeklyReport({
        startDate: formatDate(monday),
        endDate: formatDate(sunday),
      });
      reportData.value = data;
    } catch (error) {
      console.error("리포트 조회 실패:", error);
      // 에러 시 더미 데이터 혹은 초기화 (옵션)
    } finally {
      isLoading.value = false;
    }
  };

  // 주 변경 핸들러
  const changeWeek = (newDate: Date) => {
    baseDate.value = newDate;
    fetchReport();
  };

  // 초기 실행: 지난주
  // 요구사항: "오늘 날짜 기준으로 그 전 주 월요일~일요일"
  onMounted(() => {
    // 오늘 기준 지난주로 설정하려면:
    const today = new Date();
    const lastWeek = new Date(today.getFullYear(), today.getMonth(), today.getDate() - 7);
    baseDate.value = lastWeek;
    
    fetchReport();
  });

  return {
    reportData,
    isLoading,
    currentWeek,
    displayMonth,
    changeWeek,
    baseDate
  };
};