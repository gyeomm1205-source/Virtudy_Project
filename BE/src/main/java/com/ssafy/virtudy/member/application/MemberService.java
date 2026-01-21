package com.ssafy.virtudy.member.application;

import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.dto.MemberProfileResponse;
import com.ssafy.virtudy.member.dto.MemberProfileUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    public MemberProfileResponse getProfile(Member member) {
        return MemberProfileResponse.from(member);
    }

    @Transactional
    public void updateProfile(Member member, MemberProfileUpdateRequest request) {
        member.updateProfile(request.getNickName(), request.getJobType());
    }
}