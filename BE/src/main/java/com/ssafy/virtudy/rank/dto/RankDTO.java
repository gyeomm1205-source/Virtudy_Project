package com.ssafy.virtudy.rank.dto;

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
        private String id;
        private int rank;
        private int score;
        private Tier tier;
    }

    public enum Tier {
        BRONZE, SILVER, GOLD, DIAMOND
    }
}
