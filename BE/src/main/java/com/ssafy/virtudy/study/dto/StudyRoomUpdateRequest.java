package com.ssafy.virtudy.study.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StudyRoomUpdateRequest {
    private String title;
    private String password;
    private String description;
}
