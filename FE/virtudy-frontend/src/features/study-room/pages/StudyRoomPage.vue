<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useStudyRoom } from '../logic/useStudyRoom'; 
import { useFocusTimer } from '../logic/useFocusTimer';
import { RoomManager } from '@/shared/api/livekit/RoomManager';
import { Track } from 'livekit-client';
import { useAuthStore } from '@/stores/authStore'; // Pinia 스토어
import StudyTimer from '@/shared/ui/StudyTimer.vue';
import FocusTimer from '@/shared/ui/FocusTimer.vue';
// 아바타 컴포넌트 & 타입
import CharacterAvatar from '@/shared/ui/avatar/CharacterAvatar.vue';
import type { AvatarConfig } from '@/shared/types/common.types';

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
    setDebugState, // [추가] 디버그 상태 설정 함수
} = useStudyRoom();

const { focusSeconds } = useFocusTimer(isDistracted);

// 4. 상태 변수
const chatMessage = ref('');
const localVideoRef = ref<HTMLVideoElement | null>(null);

// =================================================================
// 🧪 [테스트용] 아바타 데이터 & AI 상태 조작
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

// 2) AI 상태 시뮬레이션
const aiDrowsy = ref(0); // 졸음 (0/1)
const aiAbsent = ref(0); // 부재 (0/1)
const aiPhone = ref(0);  // 핸드폰 (0/1)

// [NEW] 눈 깜빡임 & 입모양 상태
const isBlinking = ref(false); // true면 눈 감음
const mouthState = ref<'closed' | 'slightly_open' | 'wide_open'>('closed');

// 3) 테스트용 조작 함수들

// 상태 토글 (0 <-> 1, true <-> false)
const toggleState = (type: 'drowsy' | 'absent' | 'phone' | 'blink') => {
    if (type === 'drowsy') aiDrowsy.value = aiDrowsy.value === 0 ? 1 : 0;
    if (type === 'absent') aiAbsent.value = aiAbsent.value === 0 ? 1 : 0;
    if (type === 'phone') aiPhone.value = aiPhone.value === 0 ? 1 : 0;
    
    // 눈 깜빡임 토글
    if (type === 'blink') isBlinking.value = !isBlinking.value;
};

// 입모양 순환 함수 (닫힘 -> 조금 벌림 -> 크게 벌림 -> 닫힘)
const cycleMouth = () => {
    if (mouthState.value === 'closed') {
        mouthState.value = 'slightly_open';
    } else if (mouthState.value === 'slightly_open') {
        mouthState.value = 'wide_open';
    } else {
        mouthState.value = 'closed';
    }
};

// =================================================================
// 🚀 핵심 로직 (입장, 비디오 연결, 채팅)
// =================================================================

// 컴포넌트가 켜지면(마운트) 자동으로 입장 시도
onMounted(() => {
    const init = async () => {
        if (!token || !roomId) {
            alert('잘못된 접근입니다.');
            router.replace('/lobby');
            return;
        }
        console.log(`🚀 입장 시도: Room=${roomId}, User=${userId}`);
        // 방 입장 (토큰 전달)
        await joinRoom(roomId, userId, token);
    };
    init();
});

// 연결 상태가 true가 되면 내 카메라(Local Video)를 붙임
watch(isConnected, (connected) => {
    if (connected) {
        nextTick().then(() => attachLocalVideo());
    }
});

// 내 카메라 화면을 HTML <video> 태그에 연결하는 함수
const attachLocalVideo = () => {
    const roomManager = RoomManager.getInstance();
    const room = roomManager.getRoom();

    if (room && room.localParticipant && localVideoRef.value) {
        // 카메라 트랙 찾기
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

// 뒤로가기 등을 했을 때 안전하게 연결 종료

onUnmounted(() => {
    const focusMinutes = Math.floor(focusSeconds.value / 60);
    leaveRoom({
        'study-time': String(focusMinutes),
    });
});
</script>

<template>
    <div class="page-container">
        <!-- 403문제가 해결되면 화면안에서 보이게 할 예정 -->
        <div style="position: absolute; top: 10px; right: 800px; z-index: 9999; display: flex; gap: 10px;">
            <StudyTimer />
            <FocusTimer :seconds="focusSeconds" />
        </div>
        <!-- 집중 타이머 로직이 잘 돌아가는지 확인하기 위해서 버튼을 넣어둠. 나중에 삭제 예정 -->
        <div style="position: absolute; top: 80px; left: 10px; z-index: 9999; background: rgba(0,0,0,0.7); padding: 10px; border-radius: 8px;">
            <p style="color: white; font-weight: bold; margin-bottom: 5px;">🤖 AI 상태 테스트</p>
            <div style="display: flex; gap: 5px;">
                <button @click="setDebugState('FOCUS')" style="background: green; color: white; padding: 5px;">FOCUS (재개)</button>
                <button @click="setDebugState('SLEEP')" style="background: orange; color: white; padding: 5px;">SLEEP (정지)</button>
                <button @click="setDebugState('PHONE')" style="background: red; color: white; padding: 5px;">PHONE (정지)</button>
                <button @click="setDebugState('AWAY')" style="background: gray; color: white; padding: 5px;">AWAY (정지)</button>
            </div>
            <p style="color: yellow; margin-top: 5px; font-size: 0.8rem;">
                현재 상태: {{ isDistracted ? '🚫 딴짓 중 (타이머 멈춤)' : '🔥 집중 중 (타이머 작동)' }}
            </p>
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
                
                <div class="debug-controls">
                    <button @click="toggleState('drowsy')" :class="{active: aiDrowsy}">😴 졸음: {{ aiDrowsy }}</button>
                    <button @click="toggleState('phone')" :class="{active: aiPhone}">📱 폰: {{ aiPhone }}</button>
                    <button @click="toggleState('absent')" :class="{active: aiAbsent}">🚫 부재: {{ aiAbsent }}</button>
                    
                    <div class="divider">|</div>
                    <button @click="toggleState('blink')" :class="{active: isBlinking}">😉 눈: {{ isBlinking ? '감음' : '뜸' }}</button>
                    <button @click="cycleMouth" class="btn-mouth">👄 입: {{ mouthState }}</button>
                </div>

                <div class="header-controls">
                    <span class="user-badge">👤 {{ userId }}</span>
                    <button @click="handleLeave" class="btn-leave">나가기</button>
                </div>
            </header>

            <main class="room-content">
                <section class="video-section">
                    <div class="video-grid">
                        
                        <div class="video-card local">
                            <video ref="localVideoRef" autoplay muted playsinline class="hidden-video"></video>
                            
                            <div class="avatar-wrapper">
                                <CharacterAvatar 
                                    :config="myAvatarConfig"
                                    :aiDrowsy="aiDrowsy"
                                    :aiPhone="aiPhone"
                                    :aiAbsent="aiAbsent"
                                    :isBlinking="isBlinking"
                                    :mouthState="mouthState"
                                />
                            </div>
                            <span class="name-tag">나 (Me)</span>
                        </div>

                        <div v-for="rt in remoteTracks" :key="rt.participantId" class="video-card remote">
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

/* 🧪 디버그 컨트롤 패널 스타일 */
.debug-controls { display: flex; gap: 8px; align-items: center; }
.debug-controls button { 
    padding: 6px 12px; 
    border: 1px solid #ddd; 
    background: #f8f9fa; 
    cursor: pointer; 
    border-radius: 6px; 
    font-size: 0.85rem;
    transition: all 0.2s;
}
.debug-controls button:hover { background: #e9ecef; }
.debug-controls button.active { background: #ff6b6b; color: white; border-color: #ff6b6b; font-weight: bold; }

/* 입모양 버튼용 스타일 */
.debug-controls .btn-mouth { background: #d1d8e0; border-color: #a5b1c2; }
.debug-controls .btn-mouth:hover { background: #a5b1c2; }
.debug-controls .divider { color: #ccc; margin: 0 4px; }

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
    display: flex;
    justify-content: center;
    align-items: center;
}
.video-card.local { border: 3px solid #2ed573; }
.hidden-video { display: none; } /* 실제 비디오 숨김 */

/* 아바타 래퍼 */
.avatar-wrapper { width: 70%; height: 70%; } /* 아바타 크기 조정 */
.name-tag { position: absolute; bottom: 10px; left: 10px; background: rgba(0, 0, 0, 0.6); color: white; padding: 4px 10px; border-radius: 20px; font-size: 0.9rem; z-index: 10; }

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