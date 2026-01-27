package com.ssafy.virtudy.study.dto;

import com.ssafy.virtudy.global.event.annotation.EnumPattern;
import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.study.domain.RoomType;
import com.ssafy.virtudy.study.domain.StudyRoom;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Schema(description = "스터디방 생성 요청 DTO")
@Getter
@NoArgsConstructor
public class StudyRoomSaveRequest {

    @Schema(description = "스터디방 제목", example = "알고리즘 스터디")
    @NotBlank
    private String title;

    @Schema(description = "스터디방 비밀번호 (비공개방일 경우 필수)", example = "1234")
    private String password;

    @Schema(description = "스터디방 설명", example = "매주 알고리즘 문제 풀이")
    private String description;

    @Schema(description = "스터디방 공개 여부 (PUBLIC, PRIVATE)", example = "PRIVATE")
    @EnumPattern(enumClass = RoomType.class, message = "방 공개 여부가 올바르지 않습니다.")
    private String type;

    public StudyRoom toEntity(Member owner) {
        return StudyRoom.builder()
                .roomId(UUID.randomUUID().toString())
                .owner(owner)
                .title(title)
                .password(password)
                .description(description)
                .type(RoomType.valueOf(type))
                .build();
    }
}
