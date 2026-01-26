package com.ssafy.virtudy.member.api;

import com.ssafy.virtudy.global.auth.annotation.CurrentMember;
import com.ssafy.virtudy.global.event.dto.ErrorResponse;
import com.ssafy.virtudy.member.application.MemberService;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.dto.MemberProfileResponse;
import com.ssafy.virtudy.member.dto.MemberProfileUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

<<<<<<< HEAD
@Tag(name = "회원 정보 API", description = "마이페이지에 필요한 회원 상세정보 API")
=======
/*
 * TODO : API 명세서 작성
 */
>>>>>>> c0ec20e ([S14P11A703-138] 스터디 로그 C)
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "내 프로필 조회", description = "현재 로그인한 회원의 프로필 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공", content = @Content(schema = @Schema(implementation = MemberProfileResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(value = "{\"status\": 401, \"error\": \"UNAUTHORIZED\", \"code\": \"REQUEST_ERROR_003\", \"message\": \"로그인 후 이용해주세요.\", \"timestamp\": \"2024-01-01T00:00:00\"}"))),
            @ApiResponse(responseCode = "404", description = "리소스 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = {
                            @ExampleObject(name = "회원 없음", value = "{\"status\": 404, \"error\": \"NOT_FOUND\", \"code\": \"MEMBER_001\", \"message\": \"존재하지 않는 사용자입니다.\", \"timestamp\": \"2024-01-01T00:00:00\"}"),
                            @ExampleObject(name = "게임 스탯 없음", value = "{\"status\": 404, \"error\": \"NOT_FOUND\", \"code\": \"MEMBER_005\", \"message\": \"사용자의 게임 상태가 존재하지 않습니다.\", \"timestamp\": \"2024-01-01T00:00:00\"}")
                    }))
    })
    @GetMapping("/profile")
    public ResponseEntity<MemberProfileResponse> getProfile(
<<<<<<< HEAD
            @Parameter(hidden = true) @CurrentMember Member member
=======
            @CurrentMember Member member
>>>>>>> 7e431b7 ([S14P11A703-105] API에 사용자 로직 추가 및 사용자 정의 Error Code 작성)
    ) {
        MemberProfileResponse response = memberService.getProfile(member);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 프로필 수정", description = "현재 로그인한 회원의 프로필 정보(닉네임, 직업)를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(value = "{\"status\": 400, \"error\": \"BAD_REQUEST\", \"code\": \"REQUEST_ERROR_001\", \"message\": \"잘못된 요청입니다.\", \"timestamp\": \"2024-01-01T00:00:00\"}"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(value = "{\"status\": 401, \"error\": \"UNAUTHORIZED\", \"code\": \"REQUEST_ERROR_003\", \"message\": \"로그인 후 이용해주세요.\", \"timestamp\": \"2024-01-01T00:00:00\"}"))),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(value = "{\"status\": 404, \"error\": \"NOT_FOUND\", \"code\": \"MEMBER_001\", \"message\": \"존재하지 않는 사용자입니다.\", \"timestamp\": \"2024-01-01T00:00:00\"}")))
    })
    @PatchMapping("/profile")
    public ResponseEntity<Void> updateProfile(
<<<<<<< HEAD
            @Parameter(hidden = true) @CurrentMember Member member,
            @Valid @RequestBody MemberProfileUpdateRequest request
=======
            @CurrentMember Member member,
            @RequestBody MemberProfileUpdateRequest request
>>>>>>> 7e431b7 ([S14P11A703-105] API에 사용자 로직 추가 및 사용자 정의 Error Code 작성)
    ) {
        memberService.updateProfile(member, request);
        return ResponseEntity.ok().build();
    }
}
