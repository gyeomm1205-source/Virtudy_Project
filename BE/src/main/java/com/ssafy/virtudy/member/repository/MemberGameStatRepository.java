package com.ssafy.virtudy.member.repository;

import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.member.domain.MemberGameStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberGameStatRepository extends JpaRepository<MemberGameStat, Long> {
    Optional<MemberGameStat> findByMember(Member member);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE MemberGameStat m SET m.totalStudyTime = m.totalStudyTime + :time, m.tierScore = m.tierScore + :score WHERE m.member.memberId = :memberId")
    void accumulateStats(@Param("memberId") String memberId, @Param("time") int time, @Param("score") int score);
}
