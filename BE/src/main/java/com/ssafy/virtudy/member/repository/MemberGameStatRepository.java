package com.ssafy.virtudy.member.repository;

import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.domain.MemberGameStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberGameStatRepository extends JpaRepository<MemberGameStat, Long> {
    Optional<MemberGameStat> findByMember(Member member);


}
