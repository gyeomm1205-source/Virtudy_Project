package com.ssafy.virtudy.member.repository;

import com.ssafy.virtudy.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByMemberId(String memberId);

    Optional<Member> findByEmail(String email);

    boolean existsByMemberId(String memberId);

}
