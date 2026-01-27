package com.ssafy.virtudy.member.repository;

import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.dto.MemberDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByMemberId(String memberId);

    List<Member> findByNickName(String nickName);

    // 기존 findAllMemberImages() 대신 사용
    // SELECT * FROM member WHERE member_id IN ('user1', 'user2', ...)
    List<Member> findByMemberIdIn(List<String> memberIds);

    Optional<Member> findByEmail(String email);

    boolean existsByMemberId(String memberId);

}
