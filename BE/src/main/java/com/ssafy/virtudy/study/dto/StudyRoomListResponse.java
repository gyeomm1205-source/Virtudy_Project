package com.ssafy.virtudy.study.dto;

import com.ssafy.virtudy.study.domain.RoomType;
import com.ssafy.virtudy.study.domain.StudyRoom;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "스터디방 목록 조회 응답 DTO")
@Getter
public class StudyRoomListResponse {

    @Schema(description = "스터디방 고유 코드 (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private final String roomId;

    @Schema(description = "스터디방 제목", example = "알고리즘 스터디")
    private final String title;

    @Schema(description = "스터디방 공개 여부 (PUBLIC, PRIVATE)", example = "PUBLIC")
    private final RoomType type;
<<<<<<< HEAD

    @Schema(description = "현재 참여 인원 수", example = "5")
=======
>>>>>>> e788f78 ([S14P11A703-136] 엔티티 최신화)
    private final int currentUser;

    public StudyRoomListResponse(StudyRoom studyRoom, int currentUser) {
        this.roomId = studyRoom.getRoomId();
        this.title = studyRoom.getTitle();
        this.type = studyRoom.getType();
        this.currentUser = currentUser;
    }
}
