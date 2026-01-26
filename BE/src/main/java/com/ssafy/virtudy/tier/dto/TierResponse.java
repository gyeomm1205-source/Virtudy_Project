package com.ssafy.virtudy.tier.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 티어 정보 응답 DTO")
public class TierResponse {

    @Schema(description = "사용자 닉네임", example = "싸피")
    private String nickname;

    @Schema(description = "티어 점수 (0 ~ 100점)", example = "85")
    private int tierScore;

    @Schema(description = "티어 등급 (BRONZE, SILVER, GOLD, PLATINUM, DIAMOND)", example = "GOLD")
    private String tierRank;

    @Schema(description = "총 누적 공부 시간 (분 단위)", example = "1200")
    private int totalStudyTime;
}
<<<<<<< HEAD
<<<<<<< HEAD
=======
>>>>>>> 5ee6548 (add: 티어 조회/갱신 API TDD)
/*
{
nickname : "싸피",
tierScore: "85",
tierRank : "GOLD",
totalStudyTime: "1200"
}
 */
<<<<<<< HEAD
=======
>>>>>>> dad092f (add: 티어 및 리포트 관련 API)
=======
>>>>>>> 5ee6548 (add: 티어 조회/갱신 API TDD)
