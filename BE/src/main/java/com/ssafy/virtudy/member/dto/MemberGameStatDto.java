package com.ssafy.virtudy.member.dto;

import lombok.Builder;
import lombok.Getter;

public class MemberGameStatDto {

    // 밖으로 돌 ID 갖고 오기
    // tierScore 갖고 와서 줄 세우기.
    @Builder
    @Getter

    public static class RedisResponse {
        private String id; // 밖으로 도는 거 ㄱㄱ UUID
        private int tierScore;
    }
}
