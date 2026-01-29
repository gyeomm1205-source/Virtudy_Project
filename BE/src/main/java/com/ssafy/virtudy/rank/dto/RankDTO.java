package com.ssafy.virtudy.rank.dto;

<<<<<<< HEAD
import com.ssafy.virtudy.member.domain.Avatar;
=======
>>>>>>> 317f96e202cdb0fc59fa575fb5cd7806f9f6905d
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
