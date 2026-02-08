package com.ssafy.virtudy.rank.controller;

import com.ssafy.virtudy.global.aop.RateLimit;
import com.ssafy.virtudy.rank.dto.RankDTO;
import com.ssafy.virtudy.rank.service.RankService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ranks")
@RequiredArgsConstructor
public class RankController {
    private final RankService rankService;

    /**
     * 전체 랭킹 조회 (페이지네이션)
     * @param page
     * @param type
     * @return
     * GET /api/ranks/
     */
    @Operation(summary ="상위 10명씩 줍니다.", description = "page 기준으로 상위 10명씩 줍니다. ")
    @GetMapping("")
    public List<RankDTO.Response> getRankList(@RequestParam(defaultValue = "0") int page, @RequestParam String type) {
        return rankService.getRankByPage(page, type);
    }

    /**
     * 상위 5명 조회
     * @param type
     * GET /api/ranks/top5
     * @return
     */
    @Operation(summary = "상위 5명 랭킹", description = "탑 5명의 랭킹을 줍니다.")
    @GetMapping("/top5")
    public List<RankDTO.Response> top5Rank(@RequestParam String type) {
        return rankService.getTop5Rank(type);
    }

    /**
     * 내 랭킹 및 팀 랭킹 조회
     * @param user
     * @param type
     * @return
     * GET /api/ranks/me
     */
    @GetMapping("/me")
    @Operation(summary = "자신의 랭킹", description = "자신의 랭킹 혹은 최애 팀 랭킹을 검색합니다.")
    public RankDTO.Response getUserRank(@AuthenticationPrincipal UserDetails user, @RequestParam String type) {
        return rankService.getUserRankById(user.getUsername(), type);
    }


    /**
     * nickname하고 roomTitle로 검색.
     * @param name
     * @param type
     * @return
     */
    @Operation(summary = "랭킹 검색", description = "닉네임 혹은 방 이름을 기준으로 랭킹을 검색합니다.")
    @GetMapping("/search")
    public List<RankDTO.Response> searchByIdRank(@RequestParam String name, @RequestParam String type) {
        return rankService.getUserRankByNickName(name, type);
    }

}
