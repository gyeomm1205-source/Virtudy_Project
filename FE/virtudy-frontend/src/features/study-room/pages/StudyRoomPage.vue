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
const userId = authStore.userId || `guest-${Math.floor(Math.random() * 1000)}`;
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
    isDistracted,
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

// -------------------------------------------------------------
// 🪟 Document PIP 관련 로직
// -------------------------------------------------------------
const isPipActive = ref(false);
const pipDashboardRef = ref<HTMLElement | null>(null); 
const pipSourceContainerRef = ref<HTMLElement | null>(null);
let pipWindow: Window | null = null;

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

// 눈 깜빡임 & 입모양 상태 (기본값)
const isBlinking = ref(false); 
const mouthState = ref<'closed' | 'slightly_open' | 'wide_open'>('closed');

// =================================================================
// 🚀 핵심 로직 (입장, 비디오 연결, 채팅)
// =================================================================

onMounted(() => {
    const init = async () => {
        if (!token || !roomId) {
            alert('잘못된 접근입니다.');
            router.replace('/lobby');
            return;
        }
        if (!authStore.userInfo) {
            await authStore.fetchUserInfo(); //입장시 유저 정보 로드
        }
        try {
            const { data } = await lobbyAPI.getRoomDetail(roomId);
            roomTitle.value = data.title || roomId;
            roomDescription.value = data.description || '방 설명이 없습니다.';
        } catch (detailError) {
            console.warn('방 정보 조회 실패:', detailError);
            roomTitle.value = roomId;
            roomDescription.value = '';
        }
        console.log(`🚀 입장 시도: Room=${roomId}, User=${userId}`);
        await joinRoom(roomId, userId, token);
    };
    init();
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
                        <span class="member-count">👤 {{ remoteTracks.length + 1 }}/6</span>
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
                        <div class="pip-btn-area">
                            <button @click="togglePip" class="btn-pip" :class="{ active: isPipActive }">
                                {{ isPipActive ? 'PIP 종료' : '미니 모드 (PIP)' }}
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
                                    :isBlinking="isBlinking"
                                    :mouthState="mouthState"
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
                                    :isBlinking="false"
                                    :mouthState="'closed'"
                                />
                            </div>
                            <div class="user-info">
                                <span class="user-name">{{ rt.participantId }} - {{ remoteParticipantStates[rt.participantId] || 'FOCUS' }}</span>
                                <span class="heart-icon" :style="{ color: getScoreColor(50) }">♥</span>
                            </div>
                        </div>
                    </div>
                </main>

                <aside class="chat-section">
                    <div class="chat-header-simple">채팅창 영역</div> <div class="chat-messages">
                        <div v-for="(msg, idx) in messages" :key="idx" class="message-bubble" :class="{ 'my-msg': msg.sender === userId, 'sys-msg': msg.type !== 'CHAT' }">
                            <div v-if="msg.type === 'CHAT'">
                                <span class="sender">{{ msg.sender }}</span>
                                <p class="text">{{ msg.data?.message || msg.message }}</p>
                            </div>
                            <div v-else class="system-text">🔔 {{ msg.type }} 이벤트</div>
                        </div>
                    </div>
                    <div class="chat-input-area">
                        <input v-model="chatMessage" @keyup.enter="handleSendChat" type="text" placeholder="메시지 입력..." />
                        <button @click="handleSendChat">전송</button>
                    </div>
                </aside>

            </div>
        </div>
    </div>
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
.room-title { margin: 0; font-size: 1.5rem; margin-bottom: 5px; }
.room-description {
    margin: 0 0 6px 0;
    font-size: 0.95rem;
    opacity: 0.9;
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
    gap: 10px;
    align-items: center;
}
.btn-leave { background: #ff4757; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer; font-weight: bold; }
.member-count { color: white; font-weight: bold; text-shadow: 1px 1px 2px rgba(0,0,0,0.5); }
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
    margin-top: 12px;
    border-top: 1px solid #f1f2f6;
    padding-top: 10px;
}
.btn-pip {
    background: #4b6584;
    color: white;
    border: none;
    padding: 8px 0;
    width: 100%;
    border-radius: 6px;
    cursor: pointer;
    font-size: 0.9rem;
    font-weight: bold;
    transition: background 0.2s;
}
.btn-pip:hover { background: #778ca3; }
.btn-pip.active { background: #2ed573; }

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

/* 채팅 */
.chat-section { 
    flex: 1; 
    min-width: 300px; 
    background-color: #FFD966;
    display: flex; 
    flex-direction: column; 
}
.chat-messages { flex: 1; overflow-y: auto; padding: 15px; display: flex; flex-direction: column; gap: 10px; }
.message-bubble { background: white; padding: 8px 12px; border-radius: 10px; max-width: 90%; align-self: flex-start; box-shadow: 0 1px 2px rgba(0,0,0,0.1); }
.message-bubble.my-msg { align-self: flex-end; background: #7bed9f; }
.message-bubble.sys-msg { align-self: center; background: none; box-shadow: none; color: #888; font-size: 0.8rem; }
.chat-input-area { padding: 15px; display: flex; gap: 10px; flex-shrink: 0; }
.chat-input-area input { flex: 1; padding: 10px; border-radius: 4px; border: 1px solid #ddd; }
.chat-input-area button { padding: 0 20px; border-radius: 4px; border: none; background: #333; color: white; cursor: pointer; }
</style>