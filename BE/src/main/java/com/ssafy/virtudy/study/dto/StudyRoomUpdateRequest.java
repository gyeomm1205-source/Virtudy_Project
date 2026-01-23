package com.ssafy.virtudy.study.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "스터디방 수정 요청 DTO")
@Getter
@NoArgsConstructor
public class StudyRoomUpdateRequest {

    @Schema(description = "변경할 스터디방 제목", example = "변경된 알고리즘 스터디")
    private String title;

    @Schema(description = "변경할 스터디방 비밀번호", example = "5678")
    private String password;

    @Schema(description = "변경할 스터디방 설명", example = "변경된 설명입니다.")
    private String description;
}
