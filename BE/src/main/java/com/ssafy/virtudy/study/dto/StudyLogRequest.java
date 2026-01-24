package com.ssafy.virtudy.study.dto;

import com.ssafy.virtudy.study.domain.StudyEventType;
import io.swagger.v3.oas.annotations.media.Schema;
<<<<<<< HEAD
import lombok.AccessLevel;
=======
>>>>>>> c0ec20e ([S14P11A703-138] 스터디 로그 C)
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
<<<<<<< HEAD
@NoArgsConstructor(access = AccessLevel.PROTECTED)
=======
@NoArgsConstructor
>>>>>>> c0ec20e ([S14P11A703-138] 스터디 로그 C)
@Schema(description = "스터디 로그 저장 요청 DTO")
public class StudyLogRequest {

    @Schema(description = "세션 ID (UUID)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sessionId;

    @Schema(description = "상태 타입 (FOCUS, SLEEP, PHONE, AWAY)", requiredMode = Schema.RequiredMode.REQUIRED)
    private StudyEventType eventType;

<<<<<<< HEAD
    @Schema(description = "이벤트 감지 시간 (ISO-8601 형식)", example = "2024-01-24T10:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
=======
    @Schema(description = "감지된 시각", requiredMode = Schema.RequiredMode.REQUIRED)
>>>>>>> c0ec20e ([S14P11A703-138] 스터디 로그 C)
    private LocalDateTime detectedAt;
}
