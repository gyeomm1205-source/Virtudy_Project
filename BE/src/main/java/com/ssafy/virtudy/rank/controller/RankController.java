package com.ssafy.virtudy.rank.controller;

import com.ssafy.virtudy.rank.dto.RankDTO;
import com.ssafy.virtudy.rank.service.RankService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rank")
@RequiredArgsConstructor
public class RankController {
    private final RankService rankService;


    public List<RankDTO.Response> getRank() {
        return rankService.loadRank();
    }

    public RankDTO.Response getUserRank(String userId) {
        return rankService.getUserRank(userId);
    }

}
