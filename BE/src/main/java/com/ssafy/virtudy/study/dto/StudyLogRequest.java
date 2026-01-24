package com.ssafy.virtudy.study.dto;

import com.ssafy.virtudy.study.domain.StudyEventType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Schema(description = "스터디 로그 저장 요청 DTO")
public class StudyLogRequest {

    @Schema(description = "세션 ID (UUID)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sessionId;

    @Schema(description = "상태 타입 (FOCUS, SLEEP, PHONE, AWAY)", requiredMode = Schema.RequiredMode.REQUIRED)
    private StudyEventType eventType;

    @Schema(description = "감지된 시각", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime detectedAt;
}
