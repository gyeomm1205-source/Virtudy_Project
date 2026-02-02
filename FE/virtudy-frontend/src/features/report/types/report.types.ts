export interface ReportResponse{
    reportId: string;
    focusDepthPercentage: number; //집중도(백분위)
    totalStudyTime: number; //총 공부시간(분)
    //오각형 데이더(0~100가정)
    endurance: number;//지구력
    focusDepth: number;//집중력
    regularity: number;//규칙성
    stability: number; //안정성
    willPower: number;//의지력
    aiComment: string; //AI 코멘트
}

export interface WeeklyReportRequest{
    startDate: string; //YYYY-MM-DD
    endDate: string; //YYYY-MM-DD
}