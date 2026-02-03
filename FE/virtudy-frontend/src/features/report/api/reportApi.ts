import axios from 'axios';
import type{ ReportResponse, WeeklyReportRequest } from '../types/report.types';
import api from '@/shared/api/axios.config';

export const getWeeklyReport = async(params: WeeklyReportRequest): Promise<ReportResponse>=>{
    const response = await api.get<ReportResponse>('/report/weekly',{
        params:{
            startDate: params.startDate,
            endDate: params.endDate,
        }
    });
    return response.data;
}
