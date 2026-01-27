package com.ssafy.virtudy.study.controller;

import com.ssafy.virtudy.global.auth.annotation.CurrentMember;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.study.dto.StudyRoomListResponse;
import com.ssafy.virtudy.study.dto.StudyRoomResponse;
import com.ssafy.virtudy.study.dto.StudyRoomSaveRequest;
import com.ssafy.virtudy.study.dto.StudyRoomUpdateRequest;
import com.ssafy.virtudy.study.service.StudyRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "스터디방 API", description = "스터디방 생성, 조회, 수정, 삭제 등 스터디방 관련 API")
@RestController
@RequestMapping("/api/study-rooms")
@RequiredArgsConstructor
public class StudyRoomController {

    private final StudyRoomService studyRoomService;

    @Operation(summary = "스터디방 생성", description = "새로운 스터디방을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스터디방 생성 성공", content = @Content(schema = @Schema(implementation = StudyRoomResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 (유효성 검사 실패, 비밀번호 규칙 위반 등)", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "최대 생성 가능 방 개수 초과 (ROOM_005)", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "비공개 방 비밀번호 누락 (ROOM_006)", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "공개 방 비밀번호 존재 (ROOM_007)", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping
    public ResponseEntity<StudyRoomResponse> createRoom(
            @Parameter(hidden = true) @CurrentMember Member member,
            @Valid @RequestBody StudyRoomSaveRequest request
    ) {
        StudyRoomResponse response = studyRoomService.createRoom(member, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "전체 스터디방 목록 조회", description = "현재 참여 가능한 모든 스터디방 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "목록 조회 성공", content = @Content(schema = @Schema(implementation = StudyRoomListResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping
    public ResponseEntity<List<StudyRoomListResponse>> getAllOpenRooms(
    ) {
        List<StudyRoomListResponse> responses = studyRoomService.findAllOpenRooms();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "내 스터디방 목록 조회", description = "내가 속한 (방장이거나 참여중인) 스터디방 목록을 최신순으로 10개까지 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "목록 조회 성공", content = @Content(schema = @Schema(implementation = StudyRoomListResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/my")
    public ResponseEntity<List<StudyRoomListResponse>> getMyRooms(
            @Parameter(hidden = true) @CurrentMember Member member
    ) {
        List<StudyRoomListResponse> responses = studyRoomService.findMyRooms(member);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "코드로 스터디방 조회", description = "고유 코드를 사용하여 특정 스터디방의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스터디방 조회 성공", content = @Content(schema = @Schema(implementation = StudyRoomResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 스터디방 (ROOM_001)", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{roomId}")
    public ResponseEntity<StudyRoomResponse> getRoomByCode(
            @Parameter(description = "스터디방 고유 코드(UUID)", required = true) @PathVariable String roomId
    ) {
        StudyRoomResponse response = studyRoomService.findRoomByCode(roomId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "스터디방 정보 수정", description = "방장이 스터디방의 제목과 비밀번호를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스터디방 수정 성공"),
            @ApiResponse(responseCode = "400", description = "방장이 아님 (ROOM_008)", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 스터디방 (ROOM_001)", content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping("/{roomId}")
    public ResponseEntity<Void> updateRoom(
            @Parameter(hidden = true) @CurrentMember Member member,
            @Parameter(description = "스터디방 고유 코드(UUID)", required = true) @PathVariable String roomId,
            @RequestBody StudyRoomUpdateRequest request
    ) {
        studyRoomService.updateRoom(member, roomId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "스터디방 삭제 (종료)", description = "방장이 스터디방을 논리적으로 삭제(상태를 CLOSED로 변경)합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스터디방 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "방장이 아님 (ROOM_008)", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 스터디방 (ROOM_001)", content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(
            @Parameter(hidden = true) @CurrentMember Member member,
            @Parameter(description = "스터디방 고유 코드(UUID)", required = true) @PathVariable String roomId
    ) {
        studyRoomService.deleteRoom(member, roomId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "최애 스터디방 설정", description = "사용자가 참여한 스터디방 중 하나를 최애 방으로 설정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "최애 스터디방 설정 성공"),
            @ApiResponse(responseCode = "400", description = "참여하지 않은 방 (ROOM_004)", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 스터디방 (ROOM_001)", content = @Content(schema = @Schema(hidden = true)))
    })
    @PatchMapping("/favorite/{roomId}")
    public ResponseEntity<Void> setFavoriteRoom(
            @Parameter(hidden = true) @CurrentMember Member member,
            @Parameter(description = "스터디방 고유 코드(UUID)", required = true) @PathVariable String roomId
    ) {
        studyRoomService.setFavoriteRoom(member, roomId);
        return ResponseEntity.ok().build();
    }
}
