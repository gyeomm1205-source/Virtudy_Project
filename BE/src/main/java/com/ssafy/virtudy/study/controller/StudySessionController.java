package com.ssafy.virtudy.study.controller;

import com.ssafy.virtudy.study.dto.SessionMemberInfoResponse;
import com.ssafy.virtudy.study.dto.StudyLogRequest;
import com.ssafy.virtudy.study.service.StudySessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @PostMapping("/enter/{roomId}")
    public ResponseEntity<SessionMemberInfoResponse> enterRoom(
            @Parameter(description = "사용자 ID", required = true) @RequestHeader("X-MEMBER-ID") String memberId,
            @Parameter(description = "스터디방 고유 코드(UUID)", required = true) @PathVariable String roomId
    ) {
        SessionMemberInfoResponse response = studySessionService.enterRoom(memberId, roomId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "랜덤 스터디방 입장", description = "입장 가능한 방 중 하나를 랜덤으로 배정받아 입장합니다.")
    @PostMapping("/enter/random")
    public ResponseEntity<SessionMemberInfoResponse> enterRandomRoom(
            @Parameter(description = "사용자 ID", required = true) @RequestHeader("X-MEMBER-ID") String memberId
    ) {
        SessionMemberInfoResponse response = studySessionService.enterRandomRoom(memberId);
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
