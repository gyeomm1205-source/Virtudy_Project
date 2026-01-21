package com.ssafy.virtudy.member.dto;

import com.ssafy.virtudy.member.domain.ActiveTimeType;
import com.ssafy.virtudy.member.domain.JobType;
import com.ssafy.virtudy.member.domain.StudyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberSignUpRequest {
    @NotBlank
    private String email;      // 카카오에서 받은 이메일 (식별자)

    @NotBlank
    private String nickname;   // 사용자가 수정한 닉네임

    /**
     * 학습 성향 입력 받기 위한 곳
     * 1. 1일 평균 공부 시간
     * 2. 1일 목표 공부 시간
     * 3. 주 공부 시간대
     * 4. 스프린터 vs 마라토너
     */
    @NotNull
    private StudyType studyType;  // "MARATHON", "SPRINTER" 등 (Enum 추천하지만 일단 String)

    private ActiveTimeType activeTimeType; // 새벽/ 오전/ 오후/ 저녁

    private JobType jobType;

    private int targetHours; // 목표 시간
}