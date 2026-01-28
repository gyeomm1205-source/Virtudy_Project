package com.ssafy.virtudy.rank.dto;

import com.ssafy.virtudy.member.domain.Avatar;
import com.ssafy.virtudy.member.dto.AvatarResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class RankDTO {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private String nickName;
        private String email;
        private int rank;
        private int score;
        private AvatarResponse avatar;
        private Tier tier;
    }

    public enum Tier {
        BRONZE, SILVER, GOLD, PLATINUM, DIAMOND
    }
}
