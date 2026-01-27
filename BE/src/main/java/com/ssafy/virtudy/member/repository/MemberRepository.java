package com.ssafy.virtudy.member.repository;

import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.dto.MemberDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByMemberId(String memberId);

    List<Member> findByNickName(String nickName);

    // MemberRepository.java
    @Query("SELECT new com.ssafy.virtudy.member.dto.MemberDto(m.memberId, m.nickName, m.avatar, m.email) " +
            "FROM Member m WHERE m.memberId IN :ids")
    List<MemberDto> findMemberInfoByIdIn(@Param("ids") List<String> ids);


    Optional<Member> findByEmail(String email);

    boolean existsByMemberId(String memberId);

}
