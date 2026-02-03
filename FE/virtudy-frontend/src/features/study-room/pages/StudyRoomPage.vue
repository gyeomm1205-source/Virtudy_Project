<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useStudyRoom } from '../logic/useStudyRoom'; 
// [Merge] Combined Imports
import { useFocusTimer } from '../logic/useFocusTimer';
import { RoomManager } from '@/shared/api/livekit/RoomManager';
import { Track } from 'livekit-client';
import { useAuthStore } from '@/stores/authStore'; // Pinia 스토어
import StudyTimer from '@/shared/ui/StudyTimer.vue';
import FocusTimer from '@/shared/ui/FocusTimer.vue';
import CharacterAvatar from '@/shared/ui/avatar/CharacterAvatar.vue';
import type { AvatarConfig } from '@/shared/types/common.types';
import { lobbyAPI } from '@/features/lobby/api/lobbyAPI';
import type { RoomData } from '@/features/lobby/types/lobby.types';
import CreateRoomModal from '@/features/lobby/ui/CreateRoomModal.vue';
// HEAD Imports
import { useAiHandler } from '../logic/useAiHandler';
import { useStudyRoomAiStore } from '@/features/study-room/logic/useAiStore';
import { getScoreColor } from '../logic/scoreUtils'; 
import PipDashboard from '../ui/PipDashboard.vue';

// 1. 라우터 및 스토어 설정
const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 2. URL에서 정보 추출
const roomId = route.params.roomId as string;
const token = route.query.token as string;
const userId = (route.query.userId as string) || authStore.userId || `guest-${Math.floor(Math.random() * 1000)}`;
//[추가] 사용자 닉네임 표시용
const displayName = computed(() => authStore.userInfo?.nickName || userId);

// 3. 로직 훅
const { 
    joinRoom, 
    leaveRoom, 
    sendChat, 
    isConnected, 
    error, 
    messages, 
    remoteTracks,
    remoteParticipantStates,
    remoteParticipantScores, // [추가]
    remoteParticipantNames,
    isDistracted,
    roomInfoUpdate,
} = useStudyRoom();

// 3.1 AI 핸들러 & 타이머 연결 ([Merge] Both)
useAiHandler();
const aiStore = useStudyRoomAiStore();
const canRunFocusTimer = computed(() => isConnected.value && !isDistracted.value);
const { focusSeconds } = useFocusTimer(canRunFocusTimer);

// 4. 상태 변수
const chatMessage = ref('');
const localVideoRef = ref<HTMLVideoElement | null>(null);
//[추가] 방 정보
const roomTitle = ref('');
const roomDescription = ref('');
const roomDetail = ref<RoomData | null>(null);
const showEditModal = ref(false);
const roomOwnerFlag = ref(false);
const isRoomOwner = computed(() => !!roomDetail.value?.owner || roomOwnerFlag.value);

// 채팅창 열림/닫힘 상태
const isChatOpen = ref(true);

// -------------------------------------------------------------
// 🪟 Document PIP 관련 로직
// -------------------------------------------------------------
const isPipActive = ref(false);
const pipDashboardRef = ref<HTMLElement | null>(null); 
const pipSourceContainerRef = ref<HTMLElement | null>(null);
let pipWindow: Window | null = null;

// 코드 복사 버튼 관련 상태
const isHoveringCopyButton = ref(false);
const showCopyTooltip = ref(false);
const tooltipMessage = ref('코드 복사');
const isHoveringSettingsButton = ref(false);

// [PIP용 데이터] 팀원 정보 가공 (ID와 상태 점수를 넘김)
// 실제 팀원 점수 데이터가 있다면 이곳에 매핑 (현재는 Mock 65점)
const teammatesData = computed(() => {
    return remoteTracks.value.map(rt => ({
        id: rt.participantId,
        score: 65 // 예시: 팀원은 보통(노랑) 상태로 가정
    }));
});

const togglePip = async () => {
    if (isPipActive.value && pipWindow) {
        pipWindow.close();
        return;
    }

    if (!('documentPictureInPicture' in window)) {
        alert('이 기능은 Chrome/Edge 최신 버전에서만 지원됩니다.');
        return;
    }

    try {
        // 이미지 비율 고려하여 세로형 창 생성
        // @ts-ignore
        pipWindow = await window.documentPictureInPicture.requestWindow({
            width: 200, 
            height: 280,
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
            pipWindow.document.body.append(pipDashboardRef.value);
            // PIP 창 바디 스타일 (여백 제거)
            pipWindow.document.body.style.margin = '0';
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

        // [추가] 방 정보 변경을 다른 참가자에게 전파
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

// =================================================================
// 🧪 [테스트/아바타] 설정
// =================================================================

// 1) 내 아바타 설정 (백엔드 Mock Data)
const myAvatarConfig = ref<AvatarConfig>({
    hairFront: 'bang',                  
    hairBack: 'hair_back_long_straight',
    hairColor: '#3B3024',               
    eyes: 'eyes_cat',                   
    glasses: 'accessory_glasses',       
    outfit: 'outfit_knit',              
    clothesColor: '#FFD700'             
});

// 2) AI 상태 매핑 Helpers
// HEAD의 aiStore.focusStatus (FOCUS, SLEEP, PHONE, AWAY)를 Avatar Props (0/1)로 변환
const getAiDrowsy = (status: string) => (status === 'SLEEP' ? 1 : 0);
const getAiPhone = (status: string) => (status === 'PHONE' ? 1 : 0);
const getAiAbsent = (status: string) => (status === 'AWAY' ? 1 : 0);

// =================================================================
// 🚀 핵심 로직 (입장, 비디오 연결, 채팅)
// =================================================================

onMounted(() => {
    const init = async () => {
        // [수정] 로컬 테스트 지원: token이 없어도 userId가 URL에 있거나 로컬 환경이면 진행
        if (!roomId) {
            alert('잘못된 접근입니다.');
            router.replace('/lobby');
            return;
        }
        if (!authStore.userInfo) {
            await authStore.fetchUserInfo(); //입장시 유저 정보 로드
        }
        try {
            const { data } = await lobbyAPI.getRoomDetail(roomId);
            roomDetail.value = data;
            roomTitle.value = data.title || roomId;
            roomDescription.value = data.description || '방 설명이 없습니다.';
            if (!data.owner) {
                roomOwnerFlag.value = await checkRoomOwner();
            }
        } catch (detailError) {
            console.warn('방 정보 조회 실패:', detailError);
            roomDetail.value = null;
            roomOwnerFlag.value = false;
            roomTitle.value = roomId;
            roomDescription.value = '';
        }
        console.log(`🚀 입장 시도: Room=${roomId}, User=${userId}`);
        await joinRoom(roomId, userId, token, displayName.value);
    };
    init();
});

// [추가] 다른 참가자의 방 정보 업데이트 수신 처리
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
    }
});

const attachLocalVideo = () => {
    const roomManager = RoomManager.getInstance();
    const room = roomManager.getRoom();

    if (room && room.localParticipant && localVideoRef.value) {
        const publication = room.localParticipant.getTrackPublication(Track.Source.Camera);
        if (publication && publication.track) {
            publication.track.attach(localVideoRef.value);
            console.log('✅ 내 카메라 연결됨 (화면에는 숨김 처리)');
        }
    }
};

// ------------------------------------------------------------------
// 👋 퇴장 및 채팅 로직
// ------------------------------------------------------------------

const handleLeave = () => {
    if (confirm('정말 나가시겠습니까?')) {
        const focusMinutes = Math.floor(focusSeconds.value / 60);
        leaveRoom({
            'study-time': String(focusMinutes),
        });
        router.replace('/lobby'); // 로비로 이동
    }
};

const handleSendChat = () => {
    if (!chatMessage.value.trim()) return;
    sendChat(chatMessage.value);
    chatMessage.value = '';
};

const toggleChat = () => {
    isChatOpen.value = !isChatOpen.value;
};

onUnmounted(() => {
    const focusMinutes = Math.floor(focusSeconds.value / 60);
    leaveRoom({
        'study-time': String(focusMinutes),
    });
});
</script>

<template>
    <div class="page-container">
        <!-- pip 로직 추가 -->
        <div ref="pipSourceContainerRef" style="display: none;">
            <div ref="pipDashboardRef" class="pip-content-root">
                <PipDashboard 
                    :focusSeconds="focusSeconds"
                    :myAvatarConfig="myAvatarConfig"
                    :aiScore="aiStore.concentrationScore"
                    :aiStatus="aiStore.focusStatus"
                    :teammates="teammatesData"
                />
            </div>
        </div>

        <div v-if="!isConnected" class="loading-overlay">
            <div class="loading-content">
                <div v-if="error" class="error-msg">
                    <p>🚫 입장 실패</p>
                    <p>{{ error }}</p>
                    <button @click="router.replace('/lobby')" class="btn-retry">돌아가기</button>
                </div>
                <div v-else>
                    <div class="spinner"></div>
                    <p>입장 중...</p>
                </div>
            </div>
        </div>

        <div v-else class="room-layout">
            
            <div class="content-wrapper">
                
                <main class="main-window-area">
                    
                    <div class="room-info-overlay">
                        <h2 class="room-title">{{ roomTitle }}</h2>
                        <p v-if="roomDescription" class="room-description">{{ roomDescription }}</p>
                        <div class="ai-score-debug">
                            <span>🤖 AI Score: {{ Math.round(aiStore.concentrationScore) }}점</span>
                            <div class="mini-bar">
                                <div class="fill" :style="{ width: aiStore.concentrationScore + '%', background: aiStore.concentrationScore < 50 ? 'red' : 'green' }"></div>
                            </div>
                        </div>
                    </div>

                    <div class="room-controls-overlay">
                        <span class="member-count">{{ remoteTracks.length + 1 }}/6명</span>

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
                        
                        <!-- Pixel/Solid/Copy 버튼 -->
                        <div class="copy-button-container">
                            <button 
                                @click="handleCopyCode"
                                @mouseenter="handleCopyMouseEnter"
                                @mouseleave="handleCopyMouseLeave"
                                class="btn-copy-pixel"
                                :class="{ 'hover': isHoveringCopyButton }"
                            >
                                <!-- 새로운 Pixel/Solid/Copy SVG 디자인 -->
                                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none">
                                    <path d="M16 20V22H15V23H3V22H2V6H3V5H6V20H16Z" :fill="isHoveringCopyButton ? '#805143' : '#FFF2CC'"/>
                                    <path d="M22 7V18H21V19H8V18H7V2H8V1H16V7H22Z" :fill="isHoveringCopyButton ? '#805143' : '#FFF2CC'"/>
                                    <path d="M22 5V6H17V1H18V2H19V3H20V4H21V5H22Z" :fill="isHoveringCopyButton ? '#805143' : '#FFF2CC'"/>
                                </svg>
                            </button>
                            
                            <!-- 툴팁 -->
                            <div v-if="showCopyTooltip" class="copy-tooltip">
                                {{ tooltipMessage }}
                                <div class="tooltip-arrow"></div>
                            </div>
                        </div>
                        
                        <button @click="handleLeave" class="btn-leave">나가기</button>
                    </div>
                    
                    <div class="combined-timer-widget">
                        <!-- Window Frame -->
                        <div class="window-frame">
                            <div class="window-framing">
                                <div class="window-title">
                                    <span>Title</span>
                                </div>
                            </div>
                        </div>
                        
                        <!-- Timer Display -->
                        <div class="timer-display">
                            <StudyTimer />
                            <FocusTimer :seconds="focusSeconds" />
                        </div>
                    </div>

                    <div class="pip-btn-area">
                        <button @click="togglePip" class="btn-pip" :class="{ active: isPipActive }">
                            {{ isPipActive ? 'PIP 종료' : 'PIP전환' }}
                        </button>
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
                                <span class="user-name">나 ({{ displayName }})</span>
                                <span class="heart-icon" :style="{ color: getScoreColor(aiStore.concentrationScore) }">♥</span>
                            </div>
                        </div>

                        <div v-for="rt in remoteTracks" :key="rt.participantId" class="avatar-card remote">
                            <video 
                                :ref="(el) => { if(el) rt.track.attach(el as HTMLMediaElement) }"
                                autoplay playsinline 
                                class="hidden-video"
                            ></video>
                            
                            <div class="avatar-display">
                                <CharacterAvatar 
                                    :config="myAvatarConfig"
                                    :aiDrowsy="getAiDrowsy(remoteParticipantStates[rt.participantId] || 'FOCUS')" 
                                    :aiPhone="getAiPhone(remoteParticipantStates[rt.participantId] || 'FOCUS')" 
                                    :aiAbsent="getAiAbsent(remoteParticipantStates[rt.participantId] || 'FOCUS')"
                                />
                            </div>
                            <div class="user-info">
                                <span class="user-name">{{ remoteParticipantNames[rt.participantId] || rt.participantId }} - {{ remoteParticipantStates[rt.participantId] || 'FOCUS' }}</span>
                                <span class="heart-icon" :style="{ color: getScoreColor(remoteParticipantScores[rt.participantId] || 50) }">♥</span>
                            </div>
                        </div>
                    </div>
                </main>

                <!-- 채팅 영역 -->
                <aside 
                    class="flex flex-col h-full w-80 min-w-80"
                >
                    <!-- 채팅창이 열려있을 때 -->
                    <div 
                        v-if="isChatOpen" 
                        class="bg-[var(--color-choco)] h-full flex flex-col overflow-hidden"
                    >
                        <!-- 채팅 헤더 -->
                        <div class="flex items-center justify-center pt-1 px-1 pb-1 shrink-0">
                            <div class="relative w-[19.9375rem] h-[3.25rem]">
                                <!-- Window Title 배경 (Rectangle2) -->
                                <div class="absolute inset-0 bg-[var(--color-butter2)]"></div>
                                <!-- 채팅 제목 -->
                                <div class="absolute left-[0.6875rem] top-1/2 transform -translate-y-1/2">
                                    <h3 class="text-[var(--color-choco)] text-[2rem] font-['exqt'] font-medium leading-normal">채팅</h3>
                                </div>
                                <!-- X 버튼 -->
                                <button 
                                    @click="toggleChat"
                                    class="absolute right-2 top-1/2 transform -translate-y-1/2 w-6 h-6 flex items-center justify-center hover:opacity-70 transition-opacity"
                                >
                                    <svg xmlns="http://www.w3.org/2000/svg" width="2.25rem" height="2.25rem" viewBox="0 0 36 36" fill="none">
                                        <path d="M21 19.5H22.5V21H24V22.5H25.5V24H27V25.5H28.5V27H30V28.5H31.5V30H33V31.5H31.5V33H30V31.5H28.5V30H27V28.5H25.5V27H24V25.5H22.5V24H21V22.5H19.5V21H16.5V22.5H15V24H13.5V25.5H12V27H10.5V28.5H9V30H7.5V31.5H6V33H4.5V31.5H3V30H4.5V28.5H6V27H7.5V25.5H9V24H10.5V22.5H12V21H13.5V19.5H15V16.5H13.5V15H12V13.5H10.5V12H9V10.5H7.5V9H6V7.5H4.5V6H3V4.5H4.5V3H6V4.5H7.5V6H9V7.5H10.5V9H12V10.5H13.5V12H15V13.5H16.5V15H19.5V13.5H21V12H22.5V10.5H24V9H25.5V7.5H27V6H28.5V4.5H30V3H31.5V4.5H33V6H31.5V7.5H30V9H28.5V10.5H27V12H25.5V13.5H24V15H22.5V16.5H21V19.5Z" fill="#DFA67B"/>
                                    </svg>
                                </button>
                            </div>
                        </div>

                        <!-- 구분선 -->
                        <div class="h-px bg-[var(--color-choco)] opacity-80 shrink-0"></div>

                        <!-- 메시지 목록 -->
                        <div class="flex-1 overflow-y-auto p-6 space-y-2.5">
                            <div v-for="(msg, idx) in messages" :key="idx" class="flex flex-col">
                                <div v-if="msg.type === 'CHAT'">
                                    <!-- 내 메시지 -->
                                    <div v-if="msg.sender === userId" class="flex justify-end">
                                        <div class="flex flex-col items-end space-y-1.5">
                                            <div class="bg-[var(--color-butter2)] px-4 py-1 rounded-xl max-w-64">
                                                <p class="text-[var(--color-choco)] text-[1.25rem] font-['PfStardust30S'] leading-tight tracking-[-0.05rem]">{{ msg.data?.message || msg.message }}</p>
                                            </div>
                                        </div>
                                    </div>
                                    <!-- 다른 사용자 메시지 -->
                                    <div v-else class="flex flex-col space-y-1.5">
                                        <p class="text-[var(--color-cream2)] text-[0.9375rem] font-['PfStardust30S'] leading-normal tracking-[-0.0375rem]">{{ msg.sender }}</p>
                                        <div class="bg-[var(--color-syrup)] px-4 py-1 rounded-xl max-w-64">
                                            <p class="text-[var(--color-cream2)] text-[1.25rem] font-['PfStardust30S'] leading-tight tracking-[-0.05rem]">{{ msg.data?.message || msg.message }}</p>
                                        </div>
                                    </div>
                                </div>
                                <!-- 시스템 메시지 -->
                                <div v-else class="flex justify-center">
                                    <div class="text-[var(--color-cream)] text-sm opacity-70">🔔 {{ msg.type }} 이벤트</div>
                                </div>
                            </div>
                        </div>

                        <!-- 메시지 입력 박스 (Figma Frame 6 디자인) -->
                        <div class="p-[15px] shrink-0">
                            <div class="bg-[#fff8e5] border-2 border-[#fff2cc] rounded-[12px] flex items-center justify-between h-[50px] px-[20px] py-[10px]">
                                <input 
                                    v-model="chatMessage" 
                                    @keyup.enter="handleSendChat" 
                                    type="text" 
                                    placeholder="Type a message"
                                    class="flex-1 bg-transparent text-[#805143] text-[20px] font-['PfStardust30S'] leading-normal tracking-[-0.8px] placeholder:opacity-40 outline-none"
                                />
                                <button 
                                    @click="handleSendChat" 
                                    class="w-6 h-6 flex items-center justify-center hover:opacity-70 transition-opacity overflow-hidden relative"
                                >
                                    <div class="absolute translate-y-0.5 inset-[10.68%_10.66%_10.66%_10.66%]">
                                        <img alt="send" class="block max-w-none size-full" src="https://www.figma.com/api/mcp/asset/b9d70265-6324-4b42-bf69-7dfa3a62b17a" />
                                    </div>
                                </button>
                            </div>
                        </div>
                    </div>
                    
                    <!-- 채팅창이 닫혀있을 때 표시할 영역 (흰 배경) -->
                    <div 
                        v-else
                        class="flex-1 bg-white flex items-center justify-end"
                    >
                        <button 
                            @click="toggleChat"
                            class="btn-chat-open"
                        >
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 20 20" fill="none">
                                <path d="M12.5 15L7.5 10L12.5 5" stroke="#805143" stroke-width="1.66667" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </button>
                    </div>
                </aside>

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
    background-color: #f0f2f5;
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
.main-window-area { flex: 3; position: relative; background-color: #a29bfe; overflow: hidden; }
/* [NEW] 좌측 상단 방 정보 오버레이 */
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
    font-family: "PF Stardust S";
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
    font-family: 'Quicksand', 'PF Stardust S', sans-serif;
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
/* 멤버 수 표시: 피그마 디자인 정확 구현 */
.member-count {
    color: var(--text-stroke-choco, #805143);
    /* p */
    font-family: "PF Stardust S";
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

/* 설정 버튼 스타일: 피그마 디자인 정확 구현 */
.btn-settings {
    background: transparent;
    border: none;
    cursor: pointer;
    padding: 0;
    font-family: 'Quicksand', 'PF Stardust S', sans-serif;
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
.combined-timer-widget { 
    position: absolute; 
    top: 100px; 
    right: 40px; 
    width: 323px;
    height: 213.377px;
    z-index: 5; 
}

.window-frame {
    position: absolute;
    width: 323px;
    height: 200px;
    right: -10px;
    top: 80px;
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
    font-family: 'exqt', sans-serif;
    font-size: 28.805px;
    color: white;
    font-weight: normal;
    line-height: normal;
}

.timer-display {
    position: absolute;
    left: 45px;
    top: 137px;
}

.pip-btn-area {
    position: absolute;
    top: 390px;
    right: 40px;
    width: 323px;
}
.btn-pip {
    background: transparent;
    color: var(--primary-butter, #FFD966);
    border: none;
    padding: 0;
    width: 100%;
    cursor: pointer;
    font-family: exqt, sans-serif;
    font-size: 1.75rem;
    font-style: normal;
    font-weight: 500;
    line-height: normal;
    transition: opacity 0.2s;
}
.btn-pip:hover { opacity: 0.8; }
.btn-pip.active { opacity: 1; }

/* 아바타 스트립: 긴 바(Bar)*/
.avatar-strip {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    height: 50px; /* 바의 높이 */
    
    /* 긴 바의 배경색 */
    background-color:#FFC497; 
    
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
    bottom: 100px; 
    width: 140px;
    height: 140px;
}

/* 텍스트*/
.user-info {
    /* 바 내부 중앙 정렬 */
    display: flex;
    align-items: center;
    gap: 6px;
    
    color: #fff; /* 글자색 */
    font-weight: bold;
    font-size: 1rem;
    text-shadow: 1px 1px 2px rgba(0,0,0,0.3);
    /* 배경(strip)보다는 위 */
    z-index: 15; 
}

.user-name {
    max-width: 100px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.heart-icon { 
    font-size: 1.2rem; 
    transition: color 0.5s ease; /* 색상 변경 시 부드럽게 */
    margin-left: 2px;
}

.hidden-video { display: none; }

.btn-chat-open {
    display: flex;
    width: 1.5rem;
    height: 5rem;
    justify-content: center;
    align-items: center;
    border-radius: 0.875rem 0 0 0.875rem;
    border: 1px solid var(--text-stroke-choco, #805143);
    background: var(--primary-butter, #FFD966);
    box-shadow: 4px 4px 0 0 #805143;
    color: var(--text-stroke-choco, #805143);
    font-family: 'PfStardust30S', sans-serif;
    font-size: 0.875rem;
    line-height: 1;
    writing-mode: vertical-rl;
    text-orientation: mixed;
    cursor: pointer;
    transition: opacity 0.2s;
}

.btn-chat-open:hover {
    opacity: 0.85;
}


</style>