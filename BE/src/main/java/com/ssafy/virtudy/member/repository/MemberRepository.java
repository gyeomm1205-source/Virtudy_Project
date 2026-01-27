package com.ssafy.virtudy.member.repository;

import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.dto.MemberDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
<<<<<<< HEAD
<<<<<<< HEAD
=======

>>>>>>> 1538475 (feature: 랭킹 서비스 추가, (개인, 팀) 랭킹 조회, (개인, 팀) 랭킹 검색, 상위 5명 랭킹 조회, (개인, 최애팀) 랭킹 조회)
=======
>>>>>>> 343743d (fix the conflicts MemberRepository, MemberGameStatDto, RankService and RankDTO에 imageUrl 추가)
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByMemberId(String memberId);

    List<Member> findByNickName(String nickName);
<<<<<<< HEAD
<<<<<<< HEAD

    // 기존 findAllMemberImages() 대신 사용
    // SELECT * FROM member WHERE member_id IN ('user1', 'user2', ...)
    List<Member> findByMemberIdIn(List<String> memberIds);

=======
>>>>>>> 1538475 (feature: 랭킹 서비스 추가, (개인, 팀) 랭킹 조회, (개인, 팀) 랭킹 검색, 상위 5명 랭킹 조회, (개인, 최애팀) 랭킹 조회)
=======

    // MemberRepository.java
    @Query("SELECT new com.ssafy.virtudy.member.dto.MemberDto(m.memberId, m.nickName, m.avatar, m.email) " +
            "FROM Member m WHERE m.memberId IN :ids")
    List<MemberDto> findMemberInfoByIdIn(@Param("ids") List<String> ids);


>>>>>>> 343743d (fix the conflicts MemberRepository, MemberGameStatDto, RankService and RankDTO에 imageUrl 추가)
    Optional<Member> findByEmail(String email);

    boolean existsByMemberId(String memberId);

}
