import type { ReportResponse, WeeklyReportRequest } from '../types/report.types';
import api from '@/shared/api/axios.config';

export const getWeeklyReport = async(params: WeeklyReportRequest): Promise<ReportResponse> => {
    const response = await api.get<ReportResponse[]>('/report/weekly', {
        params: {
            startDate: params.startDate,
            endDate: params.endDate,
        }
    });
    // 콘솔로 응답 전체와 반환값 확인
    console.log('API 응답 데이터:', response);
    console.log('API 응답 data:', response.data);

    if (!response.data || response.data.length === 0) {
        throw new Error('리포트 데이터가 없습니다.');
    }
    console.log('반환되는 리포트:', response.data[0]);
    return response.data[0] as ReportResponse;
}
