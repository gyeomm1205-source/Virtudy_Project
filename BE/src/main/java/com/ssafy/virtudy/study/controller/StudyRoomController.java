package com.ssafy.virtudy.study.controller;

import com.ssafy.virtudy.study.dto.StudyRoomListResponse;
import com.ssafy.virtudy.study.dto.StudyRoomResponse;
import com.ssafy.virtudy.study.dto.StudyRoomSaveRequest;
import com.ssafy.virtudy.study.dto.StudyRoomUpdateRequest;
import com.ssafy.virtudy.study.service.StudyRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * TODO : Member 완성 후 parameter 수정
 * TODO : Response 형식 수정
 */
@Tag(name = "스터디방 API", description = "스터디방 생성, 조회, 수정, 삭제 등 스터디방 관련 API")
@RestController
@RequestMapping("/api/study-rooms")
@RequiredArgsConstructor
public class StudyRoomController {

    private final StudyRoomService studyRoomService;

    @Operation(summary = "스터디방 생성", description = "새로운 스터디방을 생성합니다.")
    @PostMapping
    public ResponseEntity<StudyRoomResponse> createRoom(
            @Parameter(description = "사용자 ID", required = true) @RequestHeader("X-MEMBER-ID") String memberId,
            @RequestBody StudyRoomSaveRequest request) {
        StudyRoomResponse response = studyRoomService.createRoom(memberId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "공개 스터디방 목록 조회", description = "현재 참여 가능한 모든 공개 스터디방 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<StudyRoomListResponse>> getAllOpenRooms() {
        List<StudyRoomListResponse> responses = studyRoomService.findAllOpenRooms();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "내 스터디방 목록 조회", description = "내가 속한 (방장이거나 참여중인) 스터디방 목록을 최신순으로 10개까지 조회합니다.")
    @GetMapping("/my")
    public ResponseEntity<List<StudyRoomListResponse>> getMyRooms(
            @Parameter(description = "사용자 ID", required = true) @RequestHeader("X-MEMBER-ID") String memberId) {
        List<StudyRoomListResponse> responses = studyRoomService.findMyRooms(memberId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "코드로 스터디방 조회", description = "고유 코드를 사용하여 특정 스터디방의 상세 정보를 조회합니다.")
    @GetMapping("/{roomId}")
    public ResponseEntity<StudyRoomResponse> getRoomByCode(
            @Parameter(description = "스터디방 고유 코드(UUID)", required = true) @PathVariable String roomId) {
        StudyRoomResponse response = studyRoomService.findRoomByCode(roomId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "스터디방 정보 수정", description = "방장이 스터디방의 제목과 비밀번호를 수정합니다.")
    @PatchMapping("/{roomId}")
    public ResponseEntity<Void> updateRoom(
            @Parameter(description = "사용자 ID", required = true) @RequestHeader("X-MEMBER-ID") String memberId,
            @Parameter(description = "스터디방 고유 코드(UUID)", required = true) @PathVariable String roomId,
            @RequestBody StudyRoomUpdateRequest request) {
        studyRoomService.updateRoom(memberId, roomId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "스터디방 삭제 (종료)", description = "방장이 스터디방을 논리적으로 삭제(상태를 CLOSED로 변경)합니다.")
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(
            @Parameter(description = "사용자 ID", required = true) @RequestHeader("X-MEMBER-ID") String memberId,
            @Parameter(description = "스터디방 고유 코드(UUID)", required = true) @PathVariable String roomId) {
        studyRoomService.deleteRoom(memberId, roomId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "최애 스터디방 설정", description = "사용자가 참여한 스터디방 중 하나를 최애 방으로 설정합니다.")
    @PatchMapping("/favorite/{roomId}")
    public ResponseEntity<Void> setFavoriteRoom(
            @Parameter(description = "사용자 ID", required = true) @RequestHeader("X-MEMBER-ID") String memberId,
            @Parameter(description = "스터디방 고유 코드(UUID)", required = true) @PathVariable String roomId) {
        studyRoomService.setFavoriteRoom(memberId, roomId);
        return ResponseEntity.ok().build();
    }
}
