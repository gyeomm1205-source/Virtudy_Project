package com.ssafy.virtudy.study.dto;

import com.ssafy.virtudy.study.domain.RoomType;
import com.ssafy.virtudy.study.domain.StudyRoom;
import lombok.Getter;

@Getter
public class StudyRoomResponse {
    private final String roomId;
    private final String title;
    private final String description;
    private final RoomType type;
    private final int currentUser;

    public StudyRoomResponse(StudyRoom studyRoom, int currentUser) {
        this.roomId = studyRoom.getRoomId();
        this.title = studyRoom.getTitle();
        this.description = studyRoom.getDescription();
        this.type = studyRoom.getType();
        this.currentUser = currentUser;
    }
}
