package com.ssafy.virtudy.study.dto;

import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.study.domain.RoomType;
import com.ssafy.virtudy.study.domain.StudyRoom;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class StudyRoomSaveRequest {
    private String title;
    private String password;
    private RoomType type;

    public StudyRoom toEntity(Member owner) {
        return StudyRoom.builder()
                .roomId(UUID.randomUUID().toString())
                .owner(owner)
                .title(title)
                .password(password)
                .type(type)
                .build();
    }
}
