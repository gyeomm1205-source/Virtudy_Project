<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue';
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
// HEAD Imports
import { useAiHandler } from '../logic/useAiHandler';
import { useStudyRoomAiStore } from '@/features/study-room/logic/useAiStore';

// 1. 라우터 및 스토어 설정
const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 2. URL에서 정보 추출
const roomId = route.params.roomId as string;
const token = route.query.token as string;
const userId = authStore.userId || `guest-${Math.floor(Math.random() * 1000)}`;

// 3. 로직 훅
const { 
    joinRoom, 
    leaveRoom, 
    sendChat, 
    isConnected, 
    error, 
    messages, 
    remoteTracks,
    isDistracted,
    setDebugState, 
} = useStudyRoom();

// 3.1 AI 핸들러 & 타이머 연결 ([Merge] Both)
useAiHandler();
const aiStore = useStudyRoomAiStore();
const { focusSeconds } = useFocusTimer(isDistracted);

// 4. 상태 변수
const chatMessage = ref('');
const localVideoRef = ref<HTMLVideoElement | null>(null);

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

// [NEW] 눈 깜빡임 & 입모양 상태 (기본값)
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
        <!-- 타이머 오버레이 -->
        <div style="position: absolute; top: 10px; right: 800px; z-index: 9999; display: flex; gap: 10px;">
            <StudyTimer />
            <FocusTimer :seconds="focusSeconds" />
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
            <header class="room-header">
                <h2>📚 스터디룸: {{ roomId }}</h2>
                <div class="header-controls">
                    <span class="user-badge">👤 {{ userId }}</span>
                    <button @click="handleLeave" class="btn-leave">나가기</button>
                </div>
            </header>

            <main class="room-content">
                <section class="video-section">
                    <div class="video-grid">
                        
                        <!-- 로컬 유저 (나) -->
                        <div class="video-card local">
                            <!-- 실제 비디오는 숨기고 AI 분석용으로만 송출 -->
                            <video ref="localVideoRef" autoplay muted playsinline class="hidden-video"></video>
                            
                            <!-- 아바타 표시 (AI 상태 연동) -->
                            <div class="avatar-wrapper">
                                <CharacterAvatar 
                                    :config="myAvatarConfig"
                                    :aiDrowsy="getAiDrowsy(aiStore.focusStatus)"
                                    :aiPhone="getAiPhone(aiStore.focusStatus)"
                                    :aiAbsent="getAiAbsent(aiStore.focusStatus)"
                                    :isBlinking="isBlinking"
                                    :mouthState="mouthState"
                                />
                            </div>
                            <span class="name-tag">나 (Me) - {{ aiStore.focusStatus }}</span>
                        </div>

                        <!-- 리모트 유저 (상대방) -->
                        <div v-for="rt in remoteTracks" :key="rt.participantId" class="video-card remote">
                            <!-- 상대방 비디오도 일단 숨기고 아바타? (상대 상태 데이터가 없으면 비디오 보여야 함) 
                                 [FIX] 상대방은 비디오를 보여주거나, 상대방 상태 데이터가 있다면 아바타를 보여줘야 함.
                                 일단 origin/fe 로직(무조건 아바타)을 따르되, 데이터가 없으면 기본값. 
                                 하지만 리모트 비디오가 오는데 굳이 숨기나? -> origin/fe 의도를 따름. -->
                            <video 
                                :ref="(el) => { if(el) rt.track.attach(el as HTMLMediaElement) }"
                                autoplay playsinline 
                                class="hidden-video"
                            ></video>

                            <div class="avatar-wrapper">
                                <CharacterAvatar 
                                    :config="myAvatarConfig"
                                    :aiDrowsy="0" 
                                    :aiPhone="0" 
                                    :aiAbsent="0"
                                    :isBlinking="false"
                                    :mouthState="'closed'"
                                />
                            </div>
                            <span class="name-tag">{{ rt.participantId }}</span>
                        </div>

                    </div>
                </section>

                <!-- AI 상태 디버깅/표시 패널 (HEAD 유지) -->
                <div class="ai-status-panel">
                    <h3>🤖 AI Score</h3>
                    <div class="status-item">
                        <span class="label">집중도:</span>
                        <div class="score-bar">
                            <div class="fill" :style="{ width: aiStore.concentrationScore + '%', background: aiStore.concentrationScore < 50 ? 'red' : 'green' }"></div>
                        </div>
                        <span class="value">{{ Math.round(aiStore.concentrationScore) }}점</span>
                    </div>
                </div>

                <aside class="chat-section">
                    <div class="chat-header"><h3>💬 채팅</h3></div>
                    <div class="chat-messages">
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
            </main>
        </div>
    </div>
</template>

<style scoped>
/* 페이지 기본 설정 (Nav바 가림 해결) */
.page-container {
    width: 100vw;
    height: 100vh;
    background-color: #f0f2f5;
    overflow: hidden;
    padding-top: 0; /* GlobalNavBar 숨김에 맞춰 여백 제거 */
    box-sizing: border-box;
}

/* 로딩 & 스피너 */
.loading-overlay { display: flex; justify-content: center; align-items: center; height: 100%; background: rgba(255,255,255,0.9); }
.spinner { border: 4px solid #f3f3f3; border-top: 4px solid #3498db; border-radius: 50%; width: 40px; height: 40px; animation: spin 1s linear infinite; margin: 0 auto 20px; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

/* 레이아웃 구조 */
.room-layout { display: flex; flex-direction: column; height: 100%; }
.room-header { height: 60px; background: #fff; border-bottom: 1px solid #ddd; display: flex; justify-content: space-between; align-items: center; padding: 0 20px; }
.room-content { flex: 1; display: flex; overflow: hidden; }

/* 버튼 & 뱃지 */
.btn-leave { background: #ff4757; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer; font-weight: bold; }
.user-badge { font-weight: bold; margin-right: 10px; color: #555; }

/* 비디오 영역 */
.video-section { flex: 3; background-color: #2f3542; padding: 20px; overflow-y: auto; }
.video-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; justify-content: center; }

.video-card {
    position: relative;
    background: radial-gradient(circle, #ffffff 0%, #dfe4ea 100%);
    border-radius: 12px;
    overflow: hidden;
    aspect-ratio: 16 / 9;
    box-shadow: 0 4px 10px rgba(0,0,0,0.3);
}

.video-card.local { border: 3px solid #2ed573; }
.hidden-video { display: none; } /* 실제 비디오 숨김 (AI 분석용으로 Backstage에서 돌아감) */

/* 아바타 래퍼 */
.avatar-wrapper { width: 70%; height: 70%; margin: 5% auto; } 
.name-tag { position: absolute; bottom: 10px; left: 10px; background: rgba(0, 0, 0, 0.6); color: white; padding: 4px 10px; border-radius: 20px; font-size: 0.9rem; z-index: 10; }

/* AI 상태 패널 */
.ai-status-panel {
    width: 200px;
    background: #2c3e50;
    color: white;
    padding: 15px;
    border-left: 1px solid #444;
    overflow-y: auto;
}
.ai-status-panel h3 { margin-top: 0; border-bottom: 1px solid #555; padding-bottom: 10px; font-size: 1rem; }
.status-item { margin-bottom: 15px; }
.status-item .label { display: block; font-size: 0.8rem; color: #aaa; margin-bottom: 4px; }
.score-bar { background: #555; height: 10px; border-radius: 5px; overflow: hidden; margin-bottom: 4px; }
.score-bar .fill { height: 100%; transition: width 0.3s, background-color 0.3s; }

/* 채팅 영역 */
.chat-section { flex: 1; min-width: 300px; max-width: 400px; background: white; border-left: 1px solid #ddd; display: flex; flex-direction: column; }
.chat-header { padding: 15px; border-bottom: 1px solid #eee; background: #f8f9fa; }
.chat-messages { flex: 1; overflow-y: auto; padding: 15px; background: #f1f2f6; display: flex; flex-direction: column; gap: 10px; }
.message-bubble { background: white; padding: 8px 12px; border-radius: 10px; max-width: 90%; align-self: flex-start; box-shadow: 0 1px 2px rgba(0,0,0,0.1); }
.message-bubble.my-msg { align-self: flex-end; background: #7bed9f; }
.message-bubble.sys-msg { align-self: center; background: none; box-shadow: none; color: #888; font-size: 0.8rem; }
.chat-input-area { padding: 15px; border-top: 1px solid #ddd; display: flex; gap: 10px; background: white; }
.chat-input-area input { flex: 1; padding: 10px; border: 1px solid #ddd; border-radius: 20px; outline: none; }
.chat-input-area button { padding: 0 20px; border: none; border-radius: 20px; background: #3742fa; color: white; cursor: pointer; }
</style>