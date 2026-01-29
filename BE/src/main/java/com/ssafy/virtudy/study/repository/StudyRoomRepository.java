package com.ssafy.virtudy.study.repository;

import com.ssafy.virtudy.member.domain.Member;
import com.ssafy.virtudy.study.domain.RoomStatType;
import com.ssafy.virtudy.study.domain.StudyRoom;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
