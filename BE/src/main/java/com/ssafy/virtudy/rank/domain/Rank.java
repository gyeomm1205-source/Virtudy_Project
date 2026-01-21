package com.ssafy.virtudy.rank.domain;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash("Rank")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Rank implements Serializable {

    @Id
    private Long id;

    private String nickName;

    // 순위
    private int rank;

    // 점수
    private int score;

    // 티어
    private int tier;
}
