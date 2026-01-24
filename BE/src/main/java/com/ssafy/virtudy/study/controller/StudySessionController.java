package com.ssafy.virtudy.study.controller;

import com.ssafy.virtudy.global.auth.annotation.CurrentMember;
import com.ssafy.virtudy.global.event.dto.ErrorResponse;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.study.dto.SessionMemberInfoResponse;
import com.ssafy.virtudy.study.dto.StudyLogRequest;
import com.ssafy.virtudy.study.service.StudySessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "스터디 세션 API", description = "스터디방 입장/퇴장 등 실시간 세션 관련 API")
@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class StudySessionController {

    private final StudySessionService studySessionService;

    @Operation(summary = "특정 스터디방 입장", description = "코드로 조회하거나 목록에서 선택한 특정 스터디방에 입장합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스터디방 입장 성공", content = @Content(schema = @Schema(implementation = SessionMemberInfoResponse.class))),
            @ApiResponse(responseCode = "400", description = "방이 가득 참 (ROOM_002)", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(value = "{\"status\": 400, \"error\": \"BAD_REQUEST\", \"code\": \"ROOM_002\", \"message\": \"방이 가득 찼습니다\", \"timestamp\": \"2024-01-01T00:00:00\"}"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(value = "{\"status\": 401, \"error\": \"UNAUTHORIZED\", \"code\": \"REQUEST_ERROR_003\", \"message\": \"로그인 후 이용해주세요.\", \"timestamp\": \"2024-01-01T00:00:00\"}"))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 스터디방 (ROOM_001)", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(value = "{\"status\": 404, \"error\": \"NOT_FOUND\", \"code\": \"ROOM_001\", \"message\": \"종료되었거나 존재하지 않는 방입니다.\", \"timestamp\": \"2024-01-01T00:00:00\"}")))
    })
    @PostMapping("/enter/{roomId}")
    public ResponseEntity<SessionMemberInfoResponse> enterRoom(
            @Parameter(hidden = true) @CurrentMember Member member,

            @Parameter(description = "스터디방 고유 코드(UUID)", required = true) @PathVariable String roomId
    ) {
        SessionMemberInfoResponse response = studySessionService.enterRoom(member, roomId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "랜덤 스터디방 입장", description = "입장 가능한 방 중 하나를 랜덤으로 배정받아 입장합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "랜덤 스터디방 입장 성공", content = @Content(schema = @Schema(implementation = SessionMemberInfoResponse.class))),
            @ApiResponse(responseCode = "400", description = "입장 가능한 방이 없음 (ROOM_003)", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(value = "{\"status\": 400, \"error\": \"BAD_REQUEST\", \"code\": \"ROOM_003\", \"message\": \"입장 가능한 방이 없습니다.\", \"timestamp\": \"2024-01-01T00:00:00\"}"))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                    examples = @ExampleObject(value = "{\"status\": 401, \"error\": \"UNAUTHORIZED\", \"code\": \"REQUEST_ERROR_003\", \"message\": \"로그인 후 이용해주세요.\", \"timestamp\": \"2024-01-01T00:00:00\"}")))
    })
    @PostMapping("/enter/random")
    public ResponseEntity<SessionMemberInfoResponse> enterRandomRoom(
            @Parameter(hidden = true) @CurrentMember Member member
    ) {
        SessionMemberInfoResponse response = studySessionService.enterRandomRoom(member);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "스터디 로그 저장", description = "사용자의 상태 변경(FOCUS, SLEEP, PHONE, AWAY) 로그를 저장합니다.")
    @PostMapping("/log")
    public ResponseEntity<Void> saveStudyLog(
            @Parameter(description = "사용자 ID", required = true) @RequestHeader("X-MEMBER-ID") String memberId,
            @RequestBody StudyLogRequest request
    ) {
        studySessionService.saveStudyLog(memberId, request);
        return ResponseEntity.ok().build();
    }
}
