package com.ssafy.virtudy.member.dto;

import com.ssafy.virtudy.member.domain.JobType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileUpdateRequest {
    private String nickName;
    private JobType jobType;
}
