package com.ssafy.virtudy.tier.controller;

import com.ssafy.virtudy.tier.service.TierService;
import com.ssafy.virtudy.tier.dto.TierResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ssafy.virtudy.global.auth.principal.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "Tier API", description = "티어 및 랭킹 관련 API")
@RestController
@RequestMapping("/api/tier")
@RequiredArgsConstructor
public class TierController {

    private final TierService tierService;

    /**
     * 내 티어 정보를 조회합니다.
     * 로그인된 사용자의 정보(SecurityContext)를 바탕으로 현재 티어 점수와 등급, 닉네임 등을 반환합니다.
     * 
     * @param userPrincipal 인증된 사용자 정보 (@AuthenticationPrincipal)
     * @return 내 티어 정보 (TierResponse)
     */
    @Operation(summary = "내 티어 조회", description = "로그인한 사용자의 현재 티어와 점수를 조회합니다.")
    @GetMapping("/my")
    public ResponseEntity<TierResponse> getMyTier(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        // UserPrincipal에서 username(memberId UUID)를 꺼내서 서비스 호출
        return ResponseEntity.ok(tierService.getMyTier(userPrincipal.getUsername()));
    }
}
