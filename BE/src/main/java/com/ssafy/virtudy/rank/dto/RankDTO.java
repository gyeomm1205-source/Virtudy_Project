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
        private String id;
        private String nickName;
<<<<<<< HEAD
        private String email;
=======
>>>>>>> 1538475 (feature: 랭킹 서비스 추가, (개인, 팀) 랭킹 조회, (개인, 팀) 랭킹 검색, 상위 5명 랭킹 조회, (개인, 최애팀) 랭킹 조회)
        private int rank;
        private int score;
<<<<<<< HEAD
        private AvatarResponse avatar;
=======
        private String avatarImageUrl;
>>>>>>> 343743d (fix the conflicts MemberRepository, MemberGameStatDto, RankService and RankDTO에 imageUrl 추가)
        private Tier tier;
    }

    public enum Tier {
        BRONZE, SILVER, GOLD, PLATINUM, DIAMOND
    }
}
