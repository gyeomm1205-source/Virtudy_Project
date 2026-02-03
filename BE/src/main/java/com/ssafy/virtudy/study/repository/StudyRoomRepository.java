package com.ssafy.virtudy.study.repository;

import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.study.domain.RoomStatType;
import com.ssafy.virtudy.study.domain.StudyRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudyRoomRepository extends JpaRepository<StudyRoom, Long> {
    List<StudyRoom> findAllByStatus(RoomStatType status);

    Optional<StudyRoom> findByRoomIdAndStatus(String roomId, RoomStatType status);

    int countByOwnerIdAndStatus(Long ownerId, RoomStatType status);

    List<StudyRoom> findAllByOwnerIdAndStatus(Long ownerId, RoomStatType status);

    List<StudyRoom> findStudyRoomsByRoomIdIn(List<String> roomIds);


    List<StudyRoom> findByTitle(String title);

    List<StudyRoom> findByRoomId(String roomId);

    // 랜덤 매칭을 위한 쿼리: OPEN 상태이고 인원이 6명 미만인 방을 랜덤하게 조회
    @Query("SELECT r FROM StudyRoom r " +
            "WHERE r.status = 'OPEN' " +
            "AND (SELECT COUNT(s) FROM StudySession s WHERE s.room = r AND s.endTime IS NULL) < 6 " +
            "ORDER BY function('rand')")
    List<StudyRoom> findAvailableRoomsRandomly(Pageable pageable);
}
