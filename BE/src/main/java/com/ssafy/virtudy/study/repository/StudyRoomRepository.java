package com.ssafy.virtudy.study.repository;

import com.ssafy.virtudy.study.domain.RoomStatType;
import com.ssafy.virtudy.study.domain.StudyRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyRoomRepository extends JpaRepository<StudyRoom, Long> {
    List<StudyRoom> findAllByStatus(RoomStatType status);
    Optional<StudyRoom> findByRoomIdAndStatus(String roomId, RoomStatType status);
    int countByOwnerIdAndStatus(Long ownerId, RoomStatType status);
    List<StudyRoom> findAllByOwnerId(Long ownerId);
    Optional<StudyRoom> findByRoomId(String roomId);

    List<StudyRoom> findByTitle(String title);
}
