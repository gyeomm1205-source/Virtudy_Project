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

    @Query("SELECT m.memberId, m.avatar, m.email, m.nickName FROM Member m")
    List<MemberDto> findAllMemberImages();

    Optional<Member> findByEmail(String email);

    boolean existsByMemberId(String memberId);

}
