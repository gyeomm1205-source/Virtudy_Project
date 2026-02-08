package com.ssafy.virtudy.member.repository;

import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.domain.MemberPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberPreferenceRepository extends JpaRepository<MemberPreference, Long> {
    Optional<MemberPreference> findByMember(Member member);
}
