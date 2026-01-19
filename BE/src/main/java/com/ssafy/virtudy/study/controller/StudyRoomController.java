package com.ssafy.virtudy.study.controller;

import com.ssafy.virtudy.study.dto.StudyRoomListResponse;
import com.ssafy.virtudy.study.dto.StudyRoomResponse;
import com.ssafy.virtudy.study.dto.StudyRoomSaveRequest;
import com.ssafy.virtudy.study.dto.StudyRoomUpdateRequest;
import com.ssafy.virtudy.study.service.StudyRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * TODO : Member 완성 후 parameter 수정
 * TODO : Response 형식 수정
 */
@RestController
@RequestMapping("/api/study-rooms")
@RequiredArgsConstructor
public class StudyRoomController {

    private final StudyRoomService studyRoomService;

    @PostMapping
    public ResponseEntity<StudyRoomResponse> createRoom(@RequestHeader("X-MEMBER-ID") String memberId, @RequestBody StudyRoomSaveRequest request) {
        StudyRoomResponse response = studyRoomService.createRoom(memberId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<StudyRoomListResponse>> getAllOpenRooms() {
        List<StudyRoomListResponse> responses = studyRoomService.findAllOpenRooms();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my")
    public ResponseEntity<List<StudyRoomListResponse>> getMyRooms(@RequestHeader("X-MEMBER-ID") String memberId) {
        List<StudyRoomListResponse> responses = studyRoomService.findMyRooms(memberId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<StudyRoomResponse> getRoomByCode(@PathVariable String roomId) {
        StudyRoomResponse response = studyRoomService.findRoomByCode(roomId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{roomId}")
    public ResponseEntity<Void> updateRoom(@RequestHeader("X-MEMBER-ID") String memberId, @PathVariable String roomId, @RequestBody StudyRoomUpdateRequest request) {
        studyRoomService.updateRoom(memberId, roomId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@RequestHeader("X-MEMBER-ID") String memberId, @PathVariable String roomId) {
        studyRoomService.deleteRoom(memberId, roomId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/favorite/{roomId}")
    public ResponseEntity<Void> setFavoriteRoom(@RequestHeader("X-MEMBER-ID") String memberId, @PathVariable String roomId) {
        studyRoomService.setFavoriteRoom(memberId, roomId);
        return ResponseEntity.ok().build();
    }
}
