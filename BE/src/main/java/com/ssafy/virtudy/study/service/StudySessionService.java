package com.ssafy.virtudy.study.service;

import com.ssafy.virtudy.global.config.LiveKitConfig;
import com.ssafy.virtudy.global.event.exception.BaseErrorCode;
import com.ssafy.virtudy.global.event.exception.BaseException;
import com.ssafy.virtudy.group.domain.RoomMember;
import com.ssafy.virtudy.group.repository.RoomMemberRepository;
import com.ssafy.virtudy.member.domain.*;
import com.ssafy.virtudy.member.repository.MemberRepository;
import com.ssafy.virtudy.study.domain.RoomStatType;
import com.ssafy.virtudy.study.domain.StudyRoom;
import com.ssafy.virtudy.study.domain.StudySession;
import com.ssafy.virtudy.study.dto.SessionMemberInfoResponse;
import com.ssafy.virtudy.study.repository.StudyRoomRepository;
import com.ssafy.virtudy.study.repository.StudySessionRepository;
import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudySessionService {

    private final StudySessionRepository studySessionRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final MemberRepository memberRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final LiveKitConfig liveKitConfig;

    private final static int MAX_USER = 6;
    // 후보군 크기를 50 -> 200으로 상향 조정
    // 200개 정도는 메모리 연산(거리 계산)에 큰 부하를 주지 않으면서도,
    // 랜덤 샘플링의 다양성을 확보하여 더 나은 매칭 품질을 기대할 수 있는 적절한 타협점입니다.
    private final static int RANDOM_POOL_SIZE = 200;

    public SessionMemberInfoResponse enterRoom(Member member, String roomId) {
        StudyRoom room = studyRoomRepository.findByRoomIdAndStatus(roomId, RoomStatType.OPEN)
                .orElseThrow(() -> new BaseException(BaseErrorCode.ROOM_NOT_FOUND_ERROR));

        // [Fix] Ghost Session Logic: 기존 세션이 있다면 강제 종료 후 재입장 허용
        studySessionRepository.findByMemberAndEndTimeIsNull(member)
                .ifPresent(
                studySession -> {
                    studySession.close(0);
                }
        );

        int currentUsers = studySessionRepository.findByRoomAndEndTimeIsNull(room).size();
        if (currentUsers >= MAX_USER) {
            throw new BaseException(BaseErrorCode.ROOM_FULL_ERROR);
        }

        if (!roomMemberRepository.existsByMemberAndRoom(member, room)) {
            roomMemberRepository.save(RoomMember.builder()
                    .roomMemberId(UUID.randomUUID().toString())
                    .room(room)
                    .member(member)
                    .joinedAt(LocalDateTime.now())
                    .build());
        }

        StudySession newSession = StudySession.builder()
                .member(member)
                .room(room)
                .build();
        studySessionRepository.save(newSession);

        // LiveKit 토큰 생성
        AccessToken token = new AccessToken(liveKitConfig.getLiveKitApiKey(), liveKitConfig.getLiveKitApiSecret());
        token.setName(member.getNickName());
        token.setIdentity(member.getMemberId());
        token.addGrants(new RoomJoin(true), new RoomName(roomId));

        return new SessionMemberInfoResponse(member, token.toJwt());
    }

    public SessionMemberInfoResponse enterRandomRoom(Member member) {
        // 1. DB 레벨에서 랜덤하게 후보군(200개) 조회
        List<StudyRoom> candidateRooms = studyRoomRepository.findAvailableRoomsRandomly(PageRequest.of(0, RANDOM_POOL_SIZE));

        if (candidateRooms.isEmpty()) {
            throw new BaseException(BaseErrorCode.ROOM_NOT_AVAILABLE_ERROR);
        }

        // 사용자 정보 가져오기 (가장 최근 데이터 사용)
        MemberPreference userPref = member.getPreferences().isEmpty() ? null : member.getPreferences().get(member.getPreferences().size() - 1);
        MemberGameStat userStat = member.getGameStats().isEmpty() ? null : member.getGameStats().get(member.getGameStats().size() - 1);

        // 사용자 정보가 없으면 후보군 중 하나 랜덤 선택
        if (userPref == null || userStat == null) {
            StudyRoom randomRoom = candidateRooms.get(ThreadLocalRandom.current().nextInt(candidateRooms.size()));
            return enterRoom(member, randomRoom.getRoomId());
        }

        // 2. 후보군 내에서만 거리 계산 수행
        List<Map.Entry<StudyRoom, Double>> roomDistances = new ArrayList<>();

        for (StudyRoom room : candidateRooms) {
            List<StudySession> sessions = studySessionRepository.findByRoomAndEndTimeIsNull(room);
            List<Member> roomMembers = sessions.stream().map(StudySession::getMember).collect(Collectors.toList());

            double distance = calculateRoomDistance(userPref, userStat, room, roomMembers);
            roomDistances.add(new AbstractMap.SimpleEntry<>(room, distance));
        }

        // 거리순 정렬 (오름차순)
        roomDistances.sort(Map.Entry.comparingByValue());

        // 최소 거리인 방들 찾기 (동률 처리)
        double minDistance = roomDistances.get(0).getValue();
        List<StudyRoom> bestRooms = roomDistances.stream()
                .filter(entry -> Math.abs(entry.getValue() - minDistance) < 0.0001)
                .map(Map.Entry::getKey)
                .toList();

        // 동률인 방 중 랜덤 선택
        StudyRoom selectedRoom = bestRooms.get(ThreadLocalRandom.current().nextInt(bestRooms.size()));

        return enterRoom(member, selectedRoom.getRoomId());
    }

    public void exitRoom(String memberId, int sessionRealStudyTime) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BaseException(BaseErrorCode.MEMBER_NOT_FOUND_ERROR));

        StudySession session = studySessionRepository.findByMemberAndEndTimeIsNull(member)
                .orElseThrow(() -> new BaseException(BaseErrorCode.ROOM_NOT_PARTICIPATE_ERROR));

        session.close(sessionRealStudyTime);
    }

    private double calculateRoomDistance(MemberPreference userPref, MemberGameStat userStat, StudyRoom room, List<Member> roomMembers) {
        if (roomMembers.isEmpty()) {
            return Double.MAX_VALUE; // 빈 방은 거리 최대 (사람 있는 방 우선)
        }

        // 1. Tier Score Distance
        // roomTierScore(방의 점수 합계) / 방에 입장되어 있는 사용자 수 = 평균 계산
        double roomAvgTier = (double) room.getRoomTierScore() / roomMembers.size();
        double tierDiff = Math.abs(userStat.getTierScore() - roomAvgTier);
        double normalizedTierDiff = tierDiff / 100.0; // 100점을 1단위로 정규화 (가정)

        // 2. StudyType Distance
        List<StudyType> roomStudyTypes = roomMembers.stream()
                .filter(m -> !m.getPreferences().isEmpty())
                .map(m -> m.getPreferences().get(m.getPreferences().size() - 1).getStudyType())
                .toList();
        double studyTypeDist = getEnumDistance(userPref.getStudyType(), roomStudyTypes, Enum::ordinal);

        // 3. ActiveTime Distance
        List<ActiveTimeType> roomActiveTimes = roomMembers.stream()
                .filter(m -> !m.getPreferences().isEmpty())
                .map(m -> m.getPreferences().get(m.getPreferences().size() - 1).getActiveTime())
                .toList();
        double activeTimeDist = getEnumDistance(userPref.getActiveTime(), roomActiveTimes, Enum::ordinal);

        // 4. AverageHours Distance
        List<StudyTimeCategoryType> roomAvgHours = roomMembers.stream()
                .filter(m -> !m.getPreferences().isEmpty())
                .map(m -> m.getPreferences().get(m.getPreferences().size() - 1).getAverageHours())
                .toList();
        double avgHoursDist = getEnumDistance(userPref.getAverageHours(), roomAvgHours, Enum::ordinal);

        // Vector Calculation (Euclidean Distance)
        return Math.sqrt(Math.pow(normalizedTierDiff, 2) +
                         Math.pow(studyTypeDist, 2) +
                         Math.pow(activeTimeDist, 2) +
                         Math.pow(avgHoursDist, 2));
    }

    private <T> double getEnumDistance(T userVal, List<T> roomVals, ToIntFunction<T> mapper) {
        if (roomVals.isEmpty()) return 0.0;

        // 빈도 계산
        Map<T, Long> counts = roomVals.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
        long maxCount = Collections.max(counts.values());

        // 최빈값들 추출 (동률 포함)
        List<T> modes = counts.entrySet().stream()
                .filter(e -> e.getValue() == maxCount)
                .map(Map.Entry::getKey)
                .toList();

        // 사용자 값과 최빈값들 간의 최소 거리 계산
        int userInt = mapper.applyAsInt(userVal);
        return modes.stream()
                .mapToDouble(mode -> Math.abs(userInt - mapper.applyAsInt(mode)))
                .min().orElse(0.0);
    }
}
