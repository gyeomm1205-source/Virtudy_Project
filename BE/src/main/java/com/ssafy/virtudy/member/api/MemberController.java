package com.ssafy.virtudy.member.api;

import com.ssafy.virtudy.global.auth.annotation.CurrentMember;
import com.ssafy.virtudy.member.application.MemberService;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.dto.MemberProfileResponse;
import com.ssafy.virtudy.member.dto.MemberProfileUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
 * TODO : API 명세서 작성
 */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/profile")
    public ResponseEntity<MemberProfileResponse> getProfile(
            @CurrentMember Member member
    ) {
        MemberProfileResponse response = memberService.getProfile(member);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/profile")
    public ResponseEntity<Void> updateProfile(
            @CurrentMember Member member,
            @RequestBody MemberProfileUpdateRequest request
    ) {
        memberService.updateProfile(member, request);
        return ResponseEntity.ok().build();
    }
}
