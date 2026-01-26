package com.ssafy.virtudy.study.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudyAnalysisResult {
    /** 순 공부 시간 (분 단위) - 세션의 전체 시간에서 감점 시간을 제외하거나 별도 측정된 시간 */
    private int netStudyTime;
    
    /** 졸음(SLEEP) 감지 횟수 */
    private int drowsyCount;
    
    /** 핸드폰 사용(PHONE) 감지 횟수 */
    private int phoneCount;
    
    /** 자리 비움(AWAY) 감지 횟수 */
    private int awayCount;
    
    /** 총 공부 시간 (분 단위) - 세션 시작부터 종료까지의 전체 시간 */
    private int totalStudyTime;
}
