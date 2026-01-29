package com.ssafy.virtudy.member.dto;

import com.ssafy.virtudy.global.event.annotation.EnumPattern;
import com.ssafy.virtudy.member.domain.JobType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "회원 프로필 수정 요청 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileUpdateRequest {

    @Schema(description = "새 닉네임", example = "싸피최고")
    private String nickName;

    @Schema(description = "새 직업 유형 (SCHOOL_STUDENT, UNIVERSITY_STUDENT, JOB_SEEKER, OFFICE_WORKER)", example = "JOB_SEEKER")
    @EnumPattern(enumClass = JobType.class, message = "직업 유형이 올바르지 않습니다.")
    private String jobType;
}
