package com.ssafy.virtudy.study.dto;

import com.ssafy.virtudy.study.domain.RoomType;
import com.ssafy.virtudy.study.domain.StudyRoom;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "스터디방 상세 정보 응답 DTO")
@Getter
public class StudyRoomResponse {

    @Schema(description = "스터디방 고유 코드 (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private final String roomId;

    @Schema(description = "스터디방 제목", example = "알고리즘 스터디")
    private final String title;

    @Schema(description = "스터디방 설명", example = "매주 알고리즘 문제 풀이")
    private final String description;

    @Schema(description = "스터디방 공개 여부 (PUBLIC, PRIVATE)", example = "PUBLIC")
    private final RoomType type;

    @Schema(description = "현재 참여 인원 수", example = "5")
    private final int currentUser;

    public StudyRoomResponse(StudyRoom studyRoom, int currentUser) {
        this.roomId = studyRoom.getRoomId();
        this.title = studyRoom.getTitle();
        this.description = studyRoom.getDescription();
        this.type = studyRoom.getType();
        this.currentUser = currentUser;
    }
}
