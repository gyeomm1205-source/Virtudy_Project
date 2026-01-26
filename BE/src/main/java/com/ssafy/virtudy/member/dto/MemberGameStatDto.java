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
<<<<<<< HEAD
<<<<<<< HEAD
        private String nickName;
        private String email;
<<<<<<< HEAD
=======
>>>>>>> 1538475 (feature: 랭킹 서비스 추가, (개인, 팀) 랭킹 조회, (개인, 팀) 랭킹 검색, 상위 5명 랭킹 조회, (개인, 최애팀) 랭킹 조회)
=======
        private String nickName;
>>>>>>> 343743d (fix the conflicts MemberRepository, MemberGameStatDto, RankService and RankDTO에 imageUrl 추가)
=======
>>>>>>> 188a259 (fix(rankservice) - avatar response에 추가, conflict 해결)
        private int tierScore;
    }
}
