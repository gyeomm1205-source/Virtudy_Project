package com.ssafy.virtudy.study.dto;

import com.ssafy.virtudy.study.domain.RoomType;
import com.ssafy.virtudy.study.domain.StudyRoom;
import lombok.Getter;

@Getter
public class StudyRoomListResponse {
    private final String roomId;
    private final String title;
    private final RoomType type;
    private final int currentUser;

    public StudyRoomListResponse(StudyRoom studyRoom, int currentUser) {
        this.roomId = studyRoom.getRoomId();
        this.title = studyRoom.getTitle();
        this.type = studyRoom.getType();
        this.currentUser = currentUser;
    }
}
