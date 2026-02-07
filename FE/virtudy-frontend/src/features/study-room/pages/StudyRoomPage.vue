<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router';
import { storeToRefs } from 'pinia';
import { jwtDecode } from 'jwt-decode';
import { useStudyRoom } from '../logic/useStudyRoom'; 
import { useFocusTimer } from '../logic/useFocusTimer';
import { RoomManager } from '@/shared/api/livekit/RoomManager';
import { Track } from 'livekit-client';
import { useAuthStore } from '@/stores/authStore'; // 피니아 스토어
import { useStudyStore } from '@/stores/studyStore';
import { useUiStore } from '@/stores/uiStore'; // UI 스토어

// 스터디룸 UI 컴포넌트 임포트
import StudyTimer from '@/shared/ui/StudyTimer.vue';
import FocusTimer from '@/shared/ui/FocusTimer.vue';
import CharacterAvatar from '@/shared/ui/avatar/CharacterAvatar.vue';
import StudyRoomChat from '../ui/StudyRoomChat.vue';
import PipDashboard from '../ui/PipDashboard.vue';
import FlashbangEffect from '../ui/FlashbangEffect.vue';
import WakeUpModal from '../ui/WakeUpModal.vue';
import MatchingModal from '@/shared/ui/MatchingModal.vue';
import CreateRoomModal from '@/features/lobby/ui/CreateRoomModal.vue';
import RoomBackgroundFrame from '@/features/study-room/ui/RoomBackgroundFrame.vue';

// 디버그 패널 임포트 (배포 시 제거 권장)
import DebugControls from '../ui/DebugControls.vue';

// API 임포트
import { lobbyAPI } from '@/features/lobby/api/lobbyAPI';
import type { AvatarConfig } from '@/shared/types/common.types';
import type { RoomData } from '@/features/lobby/types/lobby.types';

// HEAD Imports
import { useAiHandler } from '../logic/useAiHandler';
import { useLocalAiRunner } from '../logic/useLocalAiRunner';
import { useStudyRoomAiStore } from '@/features/study-room/logic/useAiStore';
import { getScoreColor } from '../logic/scoreUtils'; 
import { useFlashbang } from '../logic/useFlashbang';

// ==========================================================
// 라우터 및 스토어 설정
const route = useRoute();
const entryFrom = computed(() => route.query.from);
const router = useRouter();
const authStore = useAuthStore();
const studyStore = useStudyStore();
const uiStore = useUiStore(); // 스토어 사용
const { accessToken, currentRoomId } = storeToRefs(studyStore);

// URL에서 정보 추출
const roomId = route.params.roomId as string;
const userId = (route.query.userId as string) || authStore.userId || `guest-${Math.floor(Math.random() * 1000)}`;
// 사용자 닉네임 표시용
const displayName = computed(() => authStore.userInfo?.nickName || userId);

const isChatOpen = ref(true); // 채팅창 열림 / 닫힘 상태

const toggleChat = () => {
    isChatOpen.value = !isChatOpen.value;
};

const getValidStudyToken = (): string | null => {
    if (!accessToken.value || !currentRoomId.value || currentRoomId.value !== roomId) {
    studyStore.clearToken();
    router.replace('/guest');
    return null;
    }
    try {
    const decoded = jwtDecode<{ exp?: number }>(accessToken.value);
    if (decoded?.exp && Date.now() / 1000 >= decoded.exp) {
        studyStore.clearToken();
        router.replace('/guest');
        return null;
    }
    } catch (error) {
    studyStore.clearToken();
    router.replace('/guest');
    return null;
    }
    return accessToken.value;
};

// 로직 훅
const { 
    joinRoom, 
    leaveRoom, 
    sendChat, 
    isConnected, 
    error, 
    messages, 
    remoteTracks,
    remoteParticipantStates,
    remoteParticipantScores,
    remoteParticipantNames,
    remoteParticipantAvatars,
    remoteParticipantOrder,
    isDistracted,
    roomInfoUpdate,
} = useStudyRoom();

// 렌더링 에러 방지용: 유효한 트랙만 걸러내는 computed 생성
const validRemoteTracks = computed(() => {
    if (!remoteTracks.value) return [];
    // rt가 존재하고, participantId가 확실히 있는 것만 통과
    return remoteTracks.value.filter(rt => rt && rt.participantId);
});

// AI 핸들러 & 타이머 연결
useAiHandler();
const { startLocalAi, stopLocalAi } = useLocalAiRunner();
const aiStore = useStudyRoomAiStore();
const canRunFocusTimer = computed(() => isConnected.value && !isDistracted.value);
const { focusSeconds } = useFocusTimer(canRunFocusTimer);

// 상태 변수
const localVideoRef = ref<HTMLVideoElement | null>(null);
// 방 정보
const roomTitle = ref('');
const roomDescription = ref('');
const roomDetail = ref<RoomData | null>(null);
const showEditModal = ref(false);
const roomOwnerFlag = ref(false);
const isRoomOwner = computed(() => !!roomDetail.value?.owner || roomOwnerFlag.value);

// 채팅창 열림/닫힘 상태
const isRoomReady = ref(false);

watch(isConnected, async (val) => {
    if (val) {
        await nextTick();
        requestAnimationFrame(() => {
        setTimeout(() => {
            isRoomReady.value = true;
        }, 50);
        });
    } else {
        isRoomReady.value = false;
    }
}, { immediate: true });


// 섬광탄 훅 초기화 
const { 
    isStunned, 
    showSentFeedback, 
    isModalOpen, 
    drowsyParticipants, 
    isWakeUpAvailable, 
    openModal, 
    closeModal, 
    sendFlashbang,
    triggerStunEffect, // 테스트용 공개 메서드 (배포 시에는 제거 권장)
} = useFlashbang(remoteParticipantScores, remoteParticipantNames);

// -------------------------------------------------------------
// 🪟 Document PIP 관련 로직
// -------------------------------------------------------------
const isPipActive = ref(false);
const pipDashboardRef = ref<HTMLElement | null>(null); 
const pipSourceContainerRef = ref<HTMLElement | null>(null);
let pipWindow: Window | null = null;

const closePip = () => {
    if (pipWindow) {
        pipWindow.close();
        pipWindow = null;
    }
    isPipActive.value = false;
};

// 코드 복사 버튼 관련 상태
const isHoveringCopyButton = ref(false);
const showCopyTooltip = ref(false);
const tooltipMessage = ref('코드 복사');
const isHoveringSettingsButton = ref(false);

// [PIP용 데이터] 팀원 정보 가공 (ID와 상태 점수를 넘김)
// 실제 팀원 점수 데이터가 있다면 이곳에 매핑 (현재는 Mock 65점)
const teammatesData = computed(() => {
    return remoteParticipantOrder.value.map((participantId) => ({
        id: participantId,
        score: remoteParticipantScores.value[participantId] ?? 50
    }));
});

const togglePip = async () => {
    if (isPipActive.value && pipWindow) {
        pipWindow.close();
        return;
    }

    if (!('documentPictureInPicture' in window)) {
        await uiStore.openAlert('이 기능은 Chrome/Edge 최신 버전에서만 지원됩니다.', '알림');
        return;
    }

    try {
        // 이미지 비율 고려하여 세로형 창 생성
        const minPipWidth = 200;
        const minPipHeight = 300;
        // @ts-ignore
        pipWindow = await window.documentPictureInPicture.requestWindow({
        width: minPipWidth, 
        height: minPipHeight,
        });

    if (!pipWindow) return;

    // 스타일 복사
    [...document.styleSheets].forEach((styleSheet) => {
        try {
            const cssRules = [...styleSheet.cssRules].map((rule) => rule.cssText).join('');
            const style = document.createElement('style');
            style.textContent = cssRules;
            pipWindow!.document.head.appendChild(style);
        } catch (e) {
            if (styleSheet.href) {
            const link = document.createElement('link');
            link.rel = 'stylesheet';
            link.href = styleSheet.href;
            pipWindow!.document.head.appendChild(link);
            }
        }
    });

    // DOM 이동
    if (pipDashboardRef.value) {
        const pipRoot = pipDashboardRef.value;
        // 기본 표시 보장 (스타일 복사 실패 시에도 화면이 안 비도록)
        pipRoot.style.display = 'block';
        pipRoot.style.width = '100%';
        pipRoot.style.height = '100%';
        pipRoot.style.background = '#FFF4D9';

        pipWindow.document.body.append(pipRoot);
        // PIP 창 바디 스타일 (여백 제거)
        pipWindow.document.body.style.margin = '0';
        pipWindow.document.body.style.padding = '0';
        pipWindow.document.body.style.background = '#FFF4D9';
        }

    isPipActive.value = true;

    // PIP 종료 시 원복
    pipWindow.addEventListener('pagehide', () => {
        if (pipDashboardRef.value && pipSourceContainerRef.value) {
            pipSourceContainerRef.value.append(pipDashboardRef.value);
        }
        isPipActive.value = false;
        pipWindow = null;
    });

    } catch (err) {
        console.error('PIP Error:', err);
    }
};

// 코드 복사 관련 함수들
const handleCopyCode = async () => {
    try {
        await navigator.clipboard.writeText(roomId);
        tooltipMessage.value = '복사 완료!';
        showCopyTooltip.value = true;
        
        setTimeout(() => {
        showCopyTooltip.value = false;
        tooltipMessage.value = '코드 복사';
        }, 2000);
    } catch (err) {
        console.error('복사 실패:', err);
        tooltipMessage.value = '복사 실패';
        showCopyTooltip.value = true;
        setTimeout(() => {
        showCopyTooltip.value = false;
        tooltipMessage.value = '코드 복사';
        }, 2000);
    }
};

const handleCopyMouseEnter = () => {
    isHoveringCopyButton.value = true;
    if (!showCopyTooltip.value) {
        showCopyTooltip.value = true;
    }
};

const handleCopyMouseLeave = () => {
    isHoveringCopyButton.value = false;
    if (tooltipMessage.value === '코드 복사') {
        showCopyTooltip.value = false;
    }
};

const handleSettingsMouseEnter = () => {
    isHoveringSettingsButton.value = true;
};

const handleSettingsMouseLeave = () => {
    isHoveringSettingsButton.value = false;
};

const openEditRoomModal = () => {
    if (!roomDetail.value) return;
    showEditModal.value = true;
};

const handleEditSuccess = async () => {
    try {
        const { data } = await lobbyAPI.getRoomDetail(roomId);
        roomDetail.value = data;
        roomTitle.value = data.title || roomId;
        roomDescription.value = data.description || '방 설명이 없습니다.';
    if (!data.owner) {
        roomOwnerFlag.value = await checkRoomOwner();
    } else {
        roomOwnerFlag.value = false;
    }

    // 방 정보 변경을 다른 참가자에게 전파
    RoomManager.getInstance().sendControlMessage('ROOM_UPDATED', {
        roomId,
        title: roomTitle.value,
        description: roomDescription.value,
    });
    } catch (err) {
    console.error('방 정보 갱신 실패:', err);
    }
};

const checkRoomOwner = async () => {
    try {
        if (!authStore.userId) return false;
        const { data } = await lobbyAPI.getMyRooms(authStore.userId);
        const matchedRoom = data.find((room) => room.roomId === roomId);
        return !!matchedRoom?.owner;
    } catch (err) {
        console.warn('내 방 목록 조회 실패:', err);
        return false;
    }
};

// ==========================================================// 🧪 [테스트/아바타] 설정
// ==========================================================
// 내 아바타 설정 (실사용: 스토어에서 가져오기)
const myAvatarConfig = computed<AvatarConfig>(() => {
    return authStore.userInfo?.avatar ?? {
    hairFront: '',
    hairBack: '',
    hairColor: '',
    eyes: '',
    glasses: '',
    outfit: '',
    clothesColor: ''
    };
});

// AI 상태 매핑 Helpers
// HEAD의 aiStore.focusStatus (FOCUS, SLEEP, PHONE, AWAY)를 Avatar Props (0/1)로 변환
const getAiDrowsy = (status: string) => (status === 'SLEEP' ? 1 : 0);
const getAiPhone = (status: string) => (status === 'PHONE' ? 1 : 0);
const getAiAbsent = (status: string) => (status === 'AWAY' ? 1 : 0);

// 유틸리티: R값 보존을 위한 가중치 적용 음영 함수
const shiftToVividRed = (hex: string, intensity: number) => {
    const normalized = hex.replace('#', '');
    if (normalized.length !== 6) return hex;

    const num = parseInt(normalized, 16);
    const r = (num >> 16) & 255;
    const g = (num >> 8) & 255;
    const b = num & 255;

  // RGB 색 공간에서 Red 채널의 감소폭을 줄이고(0.5배), 
  // Green/Blue 채널의 감소폭을 키워(1.3배) 붉은 기와 채도를 동시에 확보함
  const rFactor = 1 - (intensity * 0.8); 
  const gbFactor = 1 - (intensity * 1.0);

  const rr = Math.max(0, Math.round(r * rFactor));
  const gg = Math.max(0, Math.round(g * gbFactor));
  const bb = Math.max(0, Math.round(b * gbFactor));

    const toHex = (c: number) => c.toString(16).padStart(2, '0');
    return `#${toHex(rr)}${toHex(gg)}${toHex(bb)}`;
};

const getHeartStyle = (score: number) => {
    const base = getScoreColor(score);
    return {
        '--heart-base': base,
        // 기존 shadeColor 대신 vivid 로직 적용
        // line: 강한 붉은 음영 (intensity 0.4)
        '--heart-line': shiftToVividRed(base, 0.4), 
        // shadow: 중간 붉은 음영 (intensity 0.25)
        '--heart-shadow': shiftToVividRed(base, 0.25), 
    };
};

type BgState = 'GREEN' | 'YELLOW' | 'RED';

const getStateFromScore = (score: number): BgState => {
    if (score > 59) return 'GREEN';
    if (score > 29) return 'YELLOW';
    return 'RED';
};

const computeAverageScore = () => {
    const scores = [aiStore.concentrationScore];
    Object.values(remoteParticipantScores.value).forEach((score) => {
        if (typeof score === 'number') scores.push(score);
    });
    if (!scores.length) return 0;
    return scores.reduce((acc, v) => acc + v, 0) / scores.length;
};

const teamAverageScore = computed<number>(() => computeAverageScore());
const bgState = computed<BgState>(() => getStateFromScore(teamAverageScore.value));

const bgHeartStyle = computed(() => {
    if (bgState.value === 'GREEN') return getHeartStyle(90);
    if (bgState.value === 'YELLOW') return getHeartStyle(70);
    return getHeartStyle(30);
});

<<<<<<< HEAD
// ==========================================================// 🚀 핵심 로직 (입장, 비디오 연결, 채팅)
// ==========================================================
onMounted(async () => {
    if (!roomId) { alert('잘못된 접근입니다.'); router.replace('/lobby'); return; }
    aiStore.setRoomId(roomId);
=======
const TEAM_AVG_INTERVAL_MS = 5000;
let teamAverageInterval: number | undefined;

// ==========================================================// 
// 🚀 핵심 로직 (입장, 비디오 연결, 채팅)
// ==========================================================
onMounted(async () => {
    if (!roomId) { 
        await uiStore.openAlert('잘못된 접근입니다.', '오류'); 
        router.replace('/lobby'); 
        return; 
    }
>>>>>>> 6e5df04c862f99ca7f21322c341ce8d2559d68f1

    const validToken = getValidStudyToken();
    if (!validToken) return;

    if (!authStore.userInfo) await authStore.fetchUserInfo();

  // 아바타 미생성 시 입장 차단
    if (!authStore.userInfo?.avatar || !authStore.userInfo.avatar.hairFront) {
        const shouldCreate = await uiStore.openAlert(
        "스터디룸에 입장하려면 아바타가 필요해요! 🎨\n지금 아바타를 만들러 가시겠어요?", 
        "아바타 생성"
    );
    if (shouldCreate) {
      router.push('/avatar/create'); // '확인' 클릭 시 아바타 생성 페이지로 이동
    } else {
      router.replace('/lobby'); // '취소' 클릭 시 로비로 이동
    }
    return; // 중요: 아래 로직(방 입장)이 실행되지 않도록 여기서 함수 종료
    }

    try {
        const { data } = await lobbyAPI.getRoomDetail(roomId);
        roomDetail.value = data;
        roomTitle.value = data.title || roomId;
        roomDescription.value = data.description || '';
        if (!data.owner) roomOwnerFlag.value = await checkRoomOwner();
    } catch {
        roomTitle.value = roomId;
    }

    await joinRoom(roomId, userId, validToken, displayName.value, myAvatarConfig.value);
    

});

onBeforeRouteLeave(() => {
    aiStore.clearRoomSession();
    aiStore.reset();
});

    // 다른 참가자의 방 정보 업데이트 수신 처리
    watch(roomInfoUpdate, (update) => {
    if (!update || update.roomId !== roomId) return;
    if (typeof update.title === 'string') {
        roomTitle.value = update.title || roomId;
    }
    if (typeof update.description === 'string') {
        roomDescription.value = update.description || '방 설명이 없습니다.';
    }
    if (roomDetail.value) {
        roomDetail.value = {
        ...roomDetail.value,
        title: roomTitle.value,
        description: roomDescription.value,
        };
    }
    });

    watch(isConnected, (connected) => {
    if (connected) {
        nextTick().then(() => attachLocalVideo());
    } else {
        stopLocalAi();
    }
    });

    const attachLocalVideo = () => {
    const roomManager = RoomManager.getInstance();
    const room = roomManager.getRoom();

    if (room && room.localParticipant && localVideoRef.value) {
        const publication = room.localParticipant.getTrackPublication(Track.Source.Camera);
        if (publication && publication.track) {
            publication.track.attach(localVideoRef.value);
            localVideoRef.value.width = 640;
            localVideoRef.value.height = 480;
            localVideoRef.value.play?.().catch(() => {});
            const start = () => startLocalAi(localVideoRef.value);
            const scheduleStart = () => setTimeout(start, 1500);
            if ('requestIdleCallback' in window) {
                window.requestIdleCallback(() => scheduleStart(), { timeout: 2000 });
            } else {
                scheduleStart();
            }
            console.log('✅ 내 카메라 연결됨 (화면에는 숨김 처리)');
        }
    }
    };

// ------------------------------------------------------------------
// 👋 퇴장 및 채팅 로직
// ------------------------------------------------------------------

    const handleLeave = async () => {
    const confirmed = await uiStore.openAlert('정말 나가시겠습니까?', '퇴장 확인');
    if (confirmed) {
        const focusMinutes = Math.floor(focusSeconds.value / 60);
        leaveRoom({
        'study-time': String(focusMinutes),
        });
        aiStore.clearRoomSession();
        aiStore.reset();
        stopLocalAi();
        studyStore.clearToken();

        // PIP 강제 종료
        closePip();

        router.replace('/lobby'); // 로비로 이동
    }
    };

onUnmounted(() => {
    const focusMinutes = Math.floor(focusSeconds.value / 60);
    stopLocalAi();
    leaveRoom({
        'study-time': String(focusMinutes),
    });
    closePip();
    });
</script>

<template>
    <div class="page-container">
        <svg class="heart-symbols" aria-hidden="true" style="position: absolute; width: 0; height: 0; overflow: hidden;">
            <symbol id="heart-pixel-symbol" viewBox="0 0 32 24">
            <g class="heart-line">
            <path d="M13.3334 2.84446H16V5.51113H13.3334V2.84446Z"/>
            <path d="M10.6667 0.177794H13.3334L13.3334 2.84446H10.6667V0.177794Z"/>
            <path d="M8.00002 0.177794H10.6667V2.84446H8.00002V0.177794Z"/>
            <path d="M5.33335 2.66667H8.00002V5.33333H5.33335V2.66667Z"/>
            <path d="M2.66669 5.51113H5.33335V8.17779H2.66669V5.51113Z"/>
            <path d="M2.66669 8H5.33335V10.6667H2.66669V8Z"/>
            <path d="M5.33335 10.6667H8.00002V13.3333H5.33335V10.6667Z"/>
            <path d="M8.00002 13.3333L10.6667 13.3333V16H8.00002L8.00002 13.3333Z"/>
            <path d="M10.6667 16H13.3334V18.6667H10.6667V16Z"/>
            <path d="M13.3334 18.6667H16V21.3333H13.3334L13.3334 18.6667Z"/>
            <path d="M16 21.3333L18.6667 21.3333V24H16V21.3333Z"/>
            <path d="M18.6667 18.6667H21.3334V21.3333L18.6667 21.3333L18.6667 18.6667Z"/>
            <path d="M21.3334 16H24V18.6667H21.3334L21.3334 16Z"/>
            <path d="M24 13.3333H26.6667V16H24V13.3333Z"/>
            <path d="M26.6667 10.6667H29.3334V13.3333L26.6667 13.3333V10.6667Z"/>
            <path d="M29.3334 8H32V10.6667H29.3334L29.3334 8Z"/>
            <path d="M29.3334 5.51113H32V8.17779H29.3334V5.51113Z"/>
            <path d="M26.6667 2.66667H29.3334V5.33333H26.6667V2.66667Z"/>
            <path d="M24 0H26.6667V2.66667H24V0Z"/>
            <path d="M21.3334 0H24V2.66667H21.3334V0Z"/>
            <path d="M18.6667 2.66667H21.3334L21.3334 5.33333H18.6667V2.66667Z"/>
            <path d="M16 5.51113H18.6667V8.17779H16V5.51113Z"/>
            </g>
            <g class="heart-highlight">
            <path d="M10.6667 5.33337H13.3333V8.00004H10.6667V5.33337Z"/>
            <path d="M8 8.00004L10.6667 8.00004L10.6667 10.6667H8V8.00004Z"/>
            </g>
            <g class="heart-base">
            <path d="M26.6666 2.66663H24V5.33329H26.6666V2.66663Z"/>
            <path d="M10.6666 13.3333H13.3333V16H10.6666V13.3333Z"/>
            <path d="M13.3333 13.3333H16V16H13.3333V13.3333Z"/>
            <path d="M16 13.3333H18.6666V16H16V13.3333Z"/>
            <path d="M16 10.6666H18.6666V13.3333H16V10.6666Z"/>
            <path d="M16 7.99996H18.6666V10.6666H16V7.99996Z"/>
            <path d="M10.6666 2.84442H13.3333V5.51109H10.6666V2.84442Z"/>
            <path d="M7.99998 2.84442H10.6666V5.51109H7.99998V2.84442Z"/>
            <path d="M5.33331 5.33329H7.99998V7.99996H5.33331V5.33329Z"/>
            <path d="M7.99998 5.33329H10.6666V7.99996H7.99998V5.33329Z"/>
            <path d="M13.3333 5.51109H16V8.17775H13.3333L13.3333 5.51109Z"/>
            <path d="M13.3333 7.99996H16V10.6666H13.3333V7.99996Z"/>
            <path d="M13.3333 10.6666H16V13.3333H13.3333L13.3333 10.6666Z"/>
            <path d="M10.6666 10.6666H13.3333L13.3333 13.3333H10.6666V10.6666Z"/>
            <path d="M10.6666 7.99996L13.3333 7.99996V10.6666H10.6666L10.6666 7.99996Z"/>
            <path d="M7.99998 10.6666H10.6666V13.3333L7.99998 13.3333V10.6666Z"/>
            <path d="M5.33331 7.99996H7.99998L7.99998 10.6666H5.33331V7.99996Z"/>
            <path d="M16 18.6666H18.6666V21.3333H16V18.6666Z"/>
            <path d="M18.6666 16H21.3333V18.6666L18.6666 18.6666V16Z"/>
            <path d="M16 16H18.6666V18.6666H16V16Z"/>
            <path d="M13.3333 16H16V18.6666L13.3333 18.6666L13.3333 16Z"/>
            <path d="M21.3333 13.3333H24V16H21.3333L21.3333 13.3333Z"/>
            <path d="M21.3333 10.6666H24V13.3333H21.3333V10.6666Z"/>
            <path d="M21.3333 7.99996H24V10.6666H21.3333V7.99996Z"/>
            <path d="M18.6666 5.33329H21.3333L21.3333 7.99996H18.6666L18.6666 5.33329Z"/>
            <path d="M18.6666 7.99996H21.3333V10.6666H18.6666V7.99996Z"/>
            <path d="M18.6666 10.6666H21.3333V13.3333H18.6666V10.6666Z"/>
            <path d="M18.6666 13.3333H21.3333L21.3333 16H18.6666V13.3333Z"/>
            <path d="M21.3333 5.33329H24L24 7.99996H21.3333L21.3333 5.33329Z"/>
            <path d="M21.3333 2.66663H24V5.33329H21.3333L21.3333 2.66663Z"/>
            <path d="M24 2.66663H26.6666V5.33329H24V2.66663Z"/>
            <path d="M24 5.33329H26.6666V7.99996L24 7.99996L24 5.33329Z"/>
            <path d="M24 7.99996L26.6666 7.99996V10.6666H24V7.99996Z"/>
            <path d="M24 10.6666H26.6666V13.3333L24 13.3333V10.6666Z"/>
            <path d="M26.6666 5.33329H29.3333V7.99996H26.6666V5.33329Z"/>
            <path d="M26.6666 7.99996H29.3333V10.6666H26.6666V7.99996Z"/>
            </g>
            <g id="heart_shadow">
            <path d="M13.3333 21.3333H16V24H13.3333V21.3333Z"/>
            <path d="M10.6667 18.6667H13.3333L13.3333 21.3333L10.6667 21.3333V18.6667Z"/>
            <path d="M16 2.84446H18.6667V5.51113H16V2.84446Z"/>
            <path d="M8 16H10.6667V18.6667H8V16Z"/>
            <path d="M5.33333 13.3333H8V16H5.33333V13.3333Z"/>
            <path d="M2.66667 10.6667H5.33333V13.3333L2.66667 13.3333V10.6667Z"/>
            <path d="M0 8H2.66667V10.6667H0V8Z"/>
            <path d="M0 5.51113H2.66667V8.17779H0V5.51113Z"/>
            <path d="M2.66667 2.84446H5.33333V5.51113H2.66667V2.84446Z"/>
            <path d="M5.33333 0.177794H8V2.84446H5.33333V0.177794Z"/>
            <path d="M18.6667 0H21.3333V2.66667H18.6667V0Z"/>
            </g>
            </symbol>
        </svg>

        <FlashbangEffect :visible="isStunned" />

        <div v-if="showSentFeedback" class="feedback-toast">
            ✨ 섬광탄 발사 완료!
        </div>

        <WakeUpModal 
            :isOpen="isModalOpen"
            :targets="drowsyParticipants"
            @close="closeModal"
            @send="sendFlashbang"
        />

        <div ref="pipSourceContainerRef" style="display: none;">
            <div ref="pipDashboardRef" class="pip-content-root">
                <PipDashboard 
                    :focusSeconds="focusSeconds"
                    :myAvatarConfig="myAvatarConfig"
                    :aiScore="aiStore.concentrationScore"
                    :aiStatus="aiStore.focusStatus"
                    :teammates="teammatesData"
                    :isWakeUpAvailable="isWakeUpAvailable"
                    :onOpenWakeUpModal="openModal"
                    :isStunned="isStunned"
                    :bgState="bgState"
                />
            </div>
        </div>

        <div v-if="!isConnected && error" class="loading-overlay">
            <div class="loading-content">
                <div class="error-msg">
                    <p>입장 실패</p>
                    <p>{{ error }}</p>
                    <button @click="router.replace('/lobby')" class="btn-retry">돌아가기</button>
                </div>
            </div>
        </div>

        <MatchingModal
            v-else-if="!isRoomReady && !entryFrom"
            title-text="입장 중..."
            subtitle-text="잠시만 기다려주세요..."
            @close="router.replace('/lobby')"
        />

        <div v-else class="room-layout">
            
            <div class="content-wrapper">
                
                <main class="main-window-area">
                    
                    <div class="room-info-overlay">
                        <h2 class="room-title flex items-center gap-2">
                            {{ roomDetail?.title || roomTitle }}
                            <span v-if="roomDetail?.type === 'PRIVATE'" class="ml-2 flex items-center">
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none">
                                    <path d="M20 12V11H19V6H18V4H17V3H16V2H14V1H10V2H8V3H7V4H6V6H5V11H4V12H3V22H4V23H20V22H21V12H20ZM8 6H9V5H10V4H14V5H15V6H16V11H8V6Z" fill="#805143"/>
                                </svg>
                            </span>
                        </h2>
                        <p v-if="roomDescription" class="room-description">{{ roomDescription }}</p>
                        <div class="ai-score-debug">
                            <span>🤖 AI Score: {{ Math.round(aiStore.concentrationScore) }}점</span>
                            <div class="mini-bar">
                                <div class="fill" :style="{ width: aiStore.concentrationScore + '%', background: aiStore.concentrationScore < 50 ? 'red' : 'green' }"></div>
                            </div>
                        </div>
                        <div>
                            <DebugControls 
                                :scores="remoteParticipantScores"
                                :names="remoteParticipantNames"
                                :isStunned="isStunned"
                                :onTriggerStun="triggerStunEffect"
                            />
                        </div>
                    </div>

                    <div class="room-controls-overlay">
                        <span class="member-count">{{ validRemoteTracks.length + 1 }}/6명</span>

                        <button
                            v-if="isRoomOwner"
                            @click="openEditRoomModal"
                            @mouseenter="handleSettingsMouseEnter"
                            @mouseleave="handleSettingsMouseLeave"
                            class="btn-settings"
                            aria-label="방 설정"
                        >
                            설정
                        </button>
                        
                        <div class="copy-button-container">
                            <button 
                                @click="handleCopyCode"
                                @mouseenter="handleCopyMouseEnter"
                                @mouseleave="handleCopyMouseLeave"
                                class="btn-copy-pixel"
                                :class="{ 'hover': isHoveringCopyButton }"
                            >
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none">
                                    <path d="M16 20V22H15V23H3V22H2V6H3V5H6V20H16Z" :fill="isHoveringCopyButton ? '#805143' : '#FFF2CC'"/>
                                    <path d="M22 7V18H21V19H8V18H7V2H8V1H16V7H22Z" :fill="isHoveringCopyButton ? '#805143' : '#FFF2CC'"/>
                                    <path d="M22 5V6H17V1H18V2H19V3H20V4H21V5H22Z" :fill="isHoveringCopyButton ? '#805143' : '#FFF2CC'"/>
                                </svg>
                            </button>
                            
                            <div v-if="showCopyTooltip" class="copy-tooltip">
                                {{ tooltipMessage }}
                                <div class="tooltip-arrow"></div>
                            </div>
                        </div>
                        
                        <button @click="handleLeave" class="btn-leave">나가기</button>
                    </div>
                    
                    <RoomBackgroundFrame :bgState="bgState" />
                    
                    <div class="timer-floating-widget">
                        <div class="hearts-container">
                            <svg class="heart-svg heart-svg--timer" :style="bgHeartStyle" viewBox="0 0 32 24">
                                <use href="#heart-pixel-symbol" />
                            </svg>
                            <svg class="heart-svg heart-svg--timer" :style="bgHeartStyle" viewBox="0 0 32 24">
                                <use href="#heart-pixel-symbol" />
                            </svg>
                            <svg class="heart-svg heart-svg--timer" :style="bgHeartStyle" viewBox="0 0 32 24">
                                <use href="#heart-pixel-symbol" />
                            </svg>
                        </div>

                        <div class="window-frame">
                            <div class="window-framing">
                                <div class="window-title">
                                    <span>집중/전체 타이머</span>
                                </div>
                            </div>
                        </div>
                        
                        <div class="timer-display">
                            <FocusTimer :seconds="focusSeconds" />
                            <StudyTimer :seconds="focusSeconds"/>
                        </div>
                        
                        <div class="pip-btn-area">
                            <button @click="togglePip" class="btn-pip btn-pixel-action">
                                {{ isPipActive ? 'PIP종료' : 'PIP전환' }}
                            </button>
                            <button 
                                class="btn-pixel-action btn-wakeup"
                                :class="{ 'active': isWakeUpAvailable }"
                                :disabled="!isWakeUpAvailable"
                                @click="openModal"
                            >
                                깨우기!
                            </button>
                        </div>
                    </div>

                    <div class="avatar-strip">
                        
                        <div class="avatar-card local">
                            <video ref="localVideoRef" autoplay muted playsinline class="hidden-video"></video>
                            
                            <div class="avatar-display">
                                <CharacterAvatar 
                                    :config="myAvatarConfig"
                                    :aiDrowsy="getAiDrowsy(aiStore.focusStatus)"
                                    :aiPhone="getAiPhone(aiStore.focusStatus)"
                                    :aiAbsent="getAiAbsent(aiStore.focusStatus)"
                                />
                            </div>
                            
                            <div class="user-info">
                                <span class="user-name">{{ displayName }}</span>
                                <svg
                                    class="heart-svg"
                                    :style="getHeartStyle(aiStore.concentrationScore)"
                                    aria-label="teammate-focus-heart"
                                    viewBox="0 0 32 24"
                                >
                                    <use href="#heart-pixel-symbol" />
                                </svg>
                            </div>
                        </div>

                        <div v-for="rt in validRemoteTracks" :key="rt.participantId" class="avatar-card remote">
                            <video 
                                :ref="(el) => { if(el && rt?.track) rt.track.attach(el as HTMLMediaElement) }"
                                autoplay playsinline 
                                class="hidden-video"
                            ></video>
                            
                            <div class="avatar-display">
                                <CharacterAvatar 
                                    :config="remoteParticipantAvatars?.[rt?.participantId] || {
                                        hairFront: 'none', hairBack: 'none', outfit: 'none', hairColor: '', clothesColor: '', eyes: 'default', glasses: 'none'
                                    }"
                                    :aiDrowsy="getAiDrowsy(remoteParticipantStates[rt?.participantId] || 'FOCUS')" 
                                    :aiPhone="getAiPhone(remoteParticipantStates[rt?.participantId] || 'FOCUS')" 
                                    :aiAbsent="getAiAbsent(remoteParticipantStates[rt?.participantId] || 'FOCUS')"
                                />
                            </div>
                            <div class="user-info">
                                <span class="user-name">{{ remoteParticipantNames[rt.participantId] || rt.participantId }}</span>
                                <svg
                                    class="heart-svg"
                                    :style="getHeartStyle(remoteParticipantScores[rt.participantId] ?? 50)"
                                    aria-label="teammate-focus-heart"
                                    viewBox="0 0 32 24"
                                >
                                    <use href="#heart-pixel-symbol" />
                                </svg>
                            </div>
                        </div>
                    </div>
                </main>

                <button 
                    @click="toggleChat" 
                    class="btn-side-toggle"
                    :class="{ 'open': isChatOpen }"
                    :title="isChatOpen ? '채팅 닫기' : '채팅 열기'"
                >
                    <svg v-if="isChatOpen" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#805143" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M9 18l6-6-6-6"/>
                    </svg>
                    
                    <svg v-else xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#805143" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M15 18l-6-6 6-6"/>
                    </svg>
                </button>

                <div class="chat-container-wrapper" :class="{ 'open': isChatOpen }">
                    <StudyRoomChat
                        :messages="messages"
                        :userId="userId"
                        :userNames="remoteParticipantNames"
                        :onSendMessage="sendChat"
                        @close="isChatOpen = false"
                    />
                </div>

            </div>
        </div>
    </div>

    <CreateRoomModal
        v-if="showEditModal"
        :initialData="roomDetail"
        @close="showEditModal = false"
        @success="handleEditSuccess"
    />
</template>

<style scoped>

/* 페이지 전체 컨테이너 */
.page-container {
    width: 100vw;
    height: 100vh;
    background: url('@/assets/room/room_wood_bg.png') center/cover no-repeat;
    background-color: #dcd0c0;
    overflow: hidden;
}

/* 로딩 오버레이 */
.loading-overlay { display: flex; justify-content: center; align-items: center; height: 100%; background: rgba(255,255,255,0.9); }
.spinner { border: 4px solid #f3f3f3; border-top: 4px solid #3498db; border-radius: 50%; width: 40px; height: 40px; animation: spin 1s linear infinite; margin: 0 auto 20px; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

/* 레이아웃 구조 */
.room-layout { display: flex; flex-direction: column; height: 100%; }

/* [수정] 컨텐츠 래퍼: 전체 화면 차지 */
.content-wrapper { display: flex; flex: 1; height: 100vh; overflow: hidden; }

/* 메인 윈도우 */
.main-window-area { 
    flex: 3; 
    position: relative; 
    background: url('@/assets/room/room_wood_bg.png') center/cover no-repeat;
    background-color: #dcd0c0; /* 이미지 로드 전 폴백 색상 */
    overflow: hidden; 
}

/* 좌측 상단 방 정보 오버레이 */
.room-info-overlay {
    position: absolute;
    top: 20px;
    left: 20px;
    z-index: 20;
    color: white; /* 배경에 따라 색상 조정 */
    text-shadow: 1px 1px 2px rgba(0,0,0,0.5);
}
/* 방 제목: 피그마 h4 디자인 정확 구현 */
.room-title {
    margin: 0;
    color: #FFF2CC;
    
    /* pixel Shadow */
    text-shadow: 4px 4px 0 #805143;
    
    /* h4 */
    font-family: Ram;
    font-size: 2.25rem;
    font-style: normal;
    font-weight: 500;
    line-height: normal;
    
    white-space: nowrap;
    margin-bottom: 6px;
}

/* 방 설명: 피그마 p 디자인 정확 구현 */
.room-description {
    margin-left: 10px;
    font-family: "PfStardust30S";
    font-size: 24px; /* 1.5rem */
    font-style: normal;
    font-weight: 400;
    line-height: normal;
    letter-spacing: 0;
    color: #805143; /* Text & Stroke (Choco) */
    white-space: nowrap;
    margin-bottom: 6px;
}
.ai-score-debug { display: flex; align-items: center; gap: 10px; font-size: 0.9rem; background: rgba(0,0,0,0.3); padding: 5px 10px; border-radius: 4px; }
.mini-bar { width: 50px; height: 6px; background: #ccc; border-radius: 3px; overflow: hidden; }
.mini-bar .fill { height: 100%; transition: width 0.3s; }

/* [NEW] 우측 상단 컨트롤 오버레이 */
.room-controls-overlay {
    position: absolute;
    top: 20px;
    right: 20px; /* 타이머 위치 고려해서 조정 필요 */
    z-index: 20;
    display: flex;
    gap: 40px;
    align-items: center;
}
/* 나가기 버튼: 피그마 디자인 정확 구현 */
.btn-leave {
    background: #FFD966; /* Primary (Butter) */
    border: 2px solid #805143; /* Text & Stroke (Choco) */
    color: #805143;
    padding: 12px 32px;
    border-radius: 2px;
    cursor: pointer;
    
    /* 피그마 폰트 스타일 */
    font-family: 'PfStardust30S', sans-serif;
    font-weight: 400;
    font-size: 24px;
    line-height: normal;
    white-space: nowrap;
    
    /* 픽셀 그림자 효과 */
    box-shadow: 4px 4px 0px 0px #805143;
    
    /* 정확한 크기 */
    width: 92px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    
    transition: all 0.1s ease;
}

.btn-leave:hover {
    transform: translate(2px, 2px);
    box-shadow: 2px 2px 0px 0px #805143;
}

.btn-leave:active {
    transform: translate(4px, 4px);
    box-shadow: none;
}
/* 멤버 수 표시 */
.member-count {
    color: var(--text-stroke-choco, #805143);
    /* p */
    font-family: "PfStardust30S";
    font-size: 1.5rem;
    font-style: normal;
    font-weight: 400;
    line-height: normal;
    white-space: nowrap;
    /* 숫자 일관성을 위한 속성 */
    font-variant-numeric: tabular-nums;
    font-feature-settings: "tnum";
    letter-spacing: 0;
}

/* 설정 버튼 스타일: */
.btn-settings {
    background: transparent;
    border: none;
    cursor: pointer;
    padding: 0;
    font-family: 'PfStardust30S', sans-serif;
    font-weight: 400;
    font-size: 24px;
    line-height: normal;
    color: #805143; /* Text & Stroke (Choco) */
    white-space: nowrap;
    transition: all 0.2s ease;
    line-height: 1;
    transition: all 0.2s ease;
}

.btn-settings:hover {
    color: #FFF2CC;
    opacity: 0.8;
    text-shadow: 1px 1px 2px rgba(0,0,0,0.7);
}

/* Pixel/Solid/Copy 버튼 스타일 */
.copy-button-container {
    position: relative;
    display: inline-block;
}

.btn-copy-pixel {
    background: transparent;
    border: none;
    cursor: pointer;
    padding: 0;
    width: 24px;
    height: 24px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
}

.btn-copy-pixel svg {
    transition: all 0.2s ease;
}

/* 툴팁 스타일 */
.copy-tooltip {
    position: absolute;
    top: calc(100% + 8px);
    left: 50%;
    transform: translateX(-50%);
    background: #333;
    color: white;
    padding: 6px 10px;
    border-radius: 4px;
    font-size: 12px;
    white-space: nowrap;
    z-index: 1000;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
    animation: tooltipFadeIn 0.2s ease-in-out;
}

.tooltip-arrow {
    position: absolute;
    bottom: 100%;
    left: 50%;
    transform: translateX(-50%);
    width: 0;
    height: 0;
    border-left: 5px solid transparent;
    border-right: 5px solid transparent;
    border-bottom: 5px solid #333;
}

@keyframes tooltipFadeIn {
    0% {
        opacity: 0;
        transform: translateX(-50%) translateY(-4px);
    }
    100% {
        opacity: 1;
        transform: translateX(-50%) translateY(0);
    }
}
/* 타이머 */
.timer-floating-widget { 
    position: absolute; 
    top: 20%; 
    right: 50px; 
    width: 323px;
    height: auto;
    z-index: 10; 
}

.hearts-container {
    display: flex;
    justify-content: flex-end; /* 우측 정렬 */
    gap: 5px;
    margin-bottom: -10px; /* 윈도우와 겹치지 않게 간격 조정 */
    margin-right: 0px;
    position: relative;
    z-index: 11; /* 윈도우보다 위에 뜨도록 */
}
.heart-svg {
    width: 30px;
    height: 30px;
    display: inline-block;
    margin-left: 2px;
}
.heart-svg--timer {
    display: block;
    margin-left: 0;
    margin-bottom: 3px;
}
.heart-line path { fill: var(--heart-line, #668128); }
.heart-base path { fill: var(--heart-base, #B8D576); }
.heart-highlight path { fill: #FFFFDB; }
.heart-shadow path,
#heart_shadow path { fill: var(--heart-shadow, var(--heart-line, #668128)); }

.window-frame {
    position: relative;
    width: 323px;
    height: 200px;
    right: -10px;
    top: 10px;
    background: #fff8e5;
}

.window-framing {
    position: absolute;
    background: #fff8e5;
    width: 100%;
    height: 100%;
    border: 2px solid #805143;
    border-radius: 2px;
    box-shadow: inset -1.029px -1.029px 0px 0px #000000,
                inset 1.029px 1.029px 0px 0px #dbdbdb,
                inset -2.057px -2.057px 0px 0px #808080,
                inset 2.057px 2.057px 0px 0px #ffffff;
}

.window-title {
    position: absolute;
    height: 30.862px;
    left: 3.09px;
    right: 3.09px;
    top: 3.09px;
    background: #805143;
    display: flex;
    align-items: center;
    padding-left: 4.11px;
}

.window-title span {
    font-family: 'Xcu', sans-serif;
    font-size: 23px;
    color: white;
    font-weight: normal;
}

/* 타이머 디스플레이 중앙 정렬 */
.timer-display {
    position: absolute;
    width: 100%; /* 전체 너비 사용 */
    top: 55%;
    left: 47%;
    transform: translate(-50%, -40%); /* 정중앙보다 약간 아래로 보정 */
    display: flex;
    flex-direction: column;
    align-items: center; /* 가로 중앙 정렬 */
    justify-content: center;
    text-align: center;
    z-index: 5;
}

.pip-btn-area {
    position: relative;
    top: 18px; /* 윈도우 프레임과의 간격 */
    width: 323px;
    display: flex;
    justify-content: center; /* 버튼 가운데 정렬 */
    gap: 8px; /* 버튼 사이 간격 */
    justify-content: flex-end; /* 우측 정렬 */
}

/* 버튼 스타일 */
.btn-pixel-action {
    background: var(--color-butter2);
    color: var(--color-choco); /* 텍스트 갈색 */
    border: 2px solid var(--color-choco); /* 진한 갈색 테두리 */
    border-radius: 50px; /* 둥근 알약 모양 */
    padding: 10px;
    cursor: pointer;
    font-family: 'PfStardust30S', sans-serif; /* 폰트 유지 */
    font-size: 1.5rem;
    line-height: normal;
    
    /* 픽셀 아트 느낌의 그림자 효과 */
    box-shadow: 0px 4px 0px var(--color-choco); 
    transform: translateY(0);
    transition: all 0.1s;
    
    /* PIP 버튼 너비 리셋 */
    width: 110px; 
    max-width: 140px;
}

/* 버튼 클릭/호버 효과 */
.btn-pixel-action:active {
    transform: translateY(4px); /* 눌리는 효과 */
    box-shadow: 0px 0px 0px var(--color-choco);
}

.btn-pixel-action:hover {
    filter: brightness(1.05);
}

/* 깨우기 버튼 스타일 */
.btn-wakeup {
    background: #e0e0e0; /* 비활성: 회색 */
    color: #999;
    border-color: #999;
    cursor: not-allowed;
    box-shadow: none;
    transform: none !important;
}

.btn-wakeup.active:active {
    transform: translateY(4px);
}

/* 활성화 상태 (노란색) */
.btn-wakeup.active {
    background: #FFD966; 
    color: #805143;
    border-color: #805143;
    cursor: pointer;
    box-shadow: 0px 4px 0px #805143;
    animation: bounce 1s infinite; /* 통통 튀는 애니메이션 */
}

@keyframes bounce {
    0%, 100% { transform: translateY(0); }
    50% { transform: translateY(-3px); }
}

.feedback-toast {
    position: fixed; top: 20%; left: 50%; transform: translateX(-50%);
    background: rgba(0,0,0,0.8); color: #FFF2CC;
    padding: 15px 30px; border-radius: 30px;
    font-family: 'PfStardust30S'; z-index: 3000;
    animation: fadeOut 2s forwards;
}
@keyframes fadeOut { 0% {opacity: 1;} 80% {opacity: 1;} 100% {opacity: 0;} }

/* 아바타 스트립: 긴 바(Bar)*/
.avatar-strip {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 50px; /* 바의 높이 */
    background-color:#FFC497; 

    background-image: url('@/assets/room/footer_bar_bg.png');
    background-repeat: no-repeat;
    background-position: center;
    background-size: cover;
    
    display: flex;
    flex-direction: row;
    align-items: center; /* 텍스트 수직 중앙 정렬 */
    padding-left: 20px; /* 왼쪽 여백 */
    gap: 0; /* 아바타 카드 간격 없음 (딱 붙음) */
    
    /* 중요: 아바타가 바 위로 튀어나와도 잘리지 않도록 함 */
    overflow-y: visible; 
    z-index: 10;
}
.avatar-strip::-webkit-scrollbar { display: none; }

/* 아바타 카드: 아바타+텍스트를 담는 투명 컨테이너 */
.avatar-card {
    position: relative;
    width: 150px; /* 한 명이 차지하는 너비 */
    display: flex;
    justify-content: center;
    align-items: center;
    /* 아바타가 겹치지 않고 나란히 */
    flex-shrink: 0;
}

/*아바타 이미지: 바 위로 올려서 배치 */
.avatar-display {
    position: absolute;
    bottom: -46px; 
    width: 200px;
    height: 215px;
    left: 7px
}

/* 텍스트 */
.user-info {
    width: 90%;
    height: 40px; /* 이름표 높이 */
    /* 이름표 PNG 배경 */
    background: url('@/assets/room/nametag_bg.png') center/100% 100% no-repeat; 
    display: flex;
    align-items: center;
    justify-content: center;
    font-family: 'PfStardust30S';
    color: #805143;
    font-size: 1.7rem;
    z-index: 11;
    position: relative;
}

.user-name {
    max-width: 110px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    margin-bottom: 3px;
}


.hidden-video {
    position: absolute;
    left: 0;
    top: 0;
    width: 160px;
    height: 120px;
    opacity: 0;
    pointer-events: none;
    z-index: -1;
}

.btn-side-toggle {
    /* 1. 위치 잡기 (화면 기준 절대 위치) */
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    z-index: 50; /* 채팅창보다 위에, 메인보다 위에 */
    /* 2. 애니메이션 (부드럽게 이동) */
    transition: right 0.3s ease; 
    right: 0;
    display: flex;
    width: 1.5rem;
    height: 5rem;
    justify-content: center;
    align-items: center;
    border-radius: 0.875rem 0 0 0.875rem; 
    
    border: 1px solid var(--text-stroke-choco, #805143);
    background: var(--color-butter2, #ffeaac);
    
    box-shadow: 4px 4px 0 0 #805143; 
    
    color: var(--text-stroke-choco, #805143);
    cursor: pointer;
}

/* 열린 상태: 채팅창 너비(20rem = 320px) 만큼 왼쪽으로 이동 */
.btn-side-toggle.open {
    right: 20rem;
}

.btn-side-toggle:hover {
    filter: brightness(1.05);
}

/* 채팅창 래퍼: 너비 애니메이션 적용 */
.chat-container-wrapper {
    width: 0;
    overflow: hidden;
    transition: width 0.3s ease; 
    flex-shrink: 0; 
}

.chat-container-wrapper.open {
    width: 20rem; /* 320px */
}

/* 부모 컨테이너가 relative여야 absolute 자식이 기준을 잡음 */
.content-wrapper {
    position: relative; 
    display: flex; 
    flex: 1; 
    height: 100vh; 
    overflow: hidden; 
}

/* 채팅 스크롤바 숨김 */
.chat-list {
    scrollbar-width: none; /* Firefox */
    -ms-overflow-style: none; /* IE/Edge */
}

.chat-list::-webkit-scrollbar {
    display: none; /* Chrome/Safari */
}


</style>