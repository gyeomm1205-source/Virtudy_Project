<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useStudyRoom } from '../logic/useStudyRoom'; 
import { RoomManager } from '@/shared/api/livekit/RoomManager';
import { Track } from 'livekit-client';
import { useAuthStore } from '@/stores/authStore'; // Pinia 스토어

// 1. 라우터 및 스토어 설정
const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// 2. URL에서 정보 추출 (방 ID, 토큰)
const roomId = route.params.roomId as string;
const token = route.query.token as string;
// 사용자 ID (스토어에서 가져오거나, 없으면 게스트)
const userId = authStore.userId || `guest-${Math.floor(Math.random() * 1000)}`;

// 3. 로직 훅 가져오기 (useStudyRoom)
const { 
    joinRoom, 
    leaveRoom, 
    sendChat, 
    isConnected, 
    error, 
    messages, 
    remoteTracks 
} = useStudyRoom();

// 4. 상태 변수
const chatMessage = ref('');
const localVideoRef = ref<HTMLVideoElement | null>(null);

// ------------------------------------------------------------------
// 🚀 핵심 로직: 방 입장 및 미디어 연결
// ------------------------------------------------------------------

// 컴포넌트가 켜지면(마운트) 자동으로 입장 시도
onMounted(async () => {
    if (!token || !roomId) {
        alert('잘못된 접근입니다. (토큰 또는 방 번호 누락)');
        router.replace('/lobby');
        return;
    }

    console.log(`🚀 입장 시도: Room=${roomId}, User=${userId}`);
    
    // 방 입장 (토큰 전달 필수!)
    await joinRoom(roomId, userId, token);
});

// 연결 상태가 true가 되면 내 카메라(Local Video)를 붙임
watch(isConnected, async (connected) => {
    if (connected) {
        await nextTick(); // DOM 렌더링 대기
        attachLocalVideo();
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
            console.log('✅ 내 카메라 화면 연결 완료');
        } else {
            console.warn('⚠️ 내 카메라 트랙을 찾을 수 없습니다. (권한 확인 필요)');
        }
    }
};

// ------------------------------------------------------------------
// 👋 퇴장 및 채팅 로직
// ------------------------------------------------------------------

const handleLeave = () => {
    if (confirm('정말 나가시겠습니까?')) {
        leaveRoom();
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
    leaveRoom();
});
</script>

<template>
    <div class="page-container">
        <div v-if="!isConnected" class="loading-overlay">
            <div class="loading-content">
                <div v-if="error" class="error-msg">
                    <p>🚫 입장 실패</p>
                    <p>{{ error }}</p>
                    <button @click="router.replace('/lobby')" class="btn-retry">로비로 돌아가기</button>
                </div>
                <div v-else>
                    <div class="spinner"></div>
                    <p>스터디룸에 입장하고 있습니다...</p>
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
                        <div class="video-card local">
                            <video ref="localVideoRef" autoplay muted playsinline></video>
                            <span class="name-tag">나 (Me)</span>
                        </div>

                        <div 
                            v-for="rt in remoteTracks" 
                            :key="rt.participantId" 
                            class="video-card remote"
                        >
                            <video 
                                :ref="(el) => { if(el) rt.track.attach(el as HTMLMediaElement) }"
                                autoplay 
                                playsinline
                            ></video>
                            <span class="name-tag">{{ rt.participantId }}</span>
                        </div>
                    </div>
                </section>

                <aside class="chat-section">
                    <div class="chat-header">
                        <h3>💬 채팅</h3>
                    </div>
                    
                    <div class="chat-messages">
                        <div 
                            v-for="(msg, idx) in messages" 
                            :key="idx" 
                            class="message-bubble"
                            :class="{ 'my-msg': msg.sender === userId, 'sys-msg': msg.type !== 'CHAT' }"
                        >
                            <div v-if="msg.type === 'CHAT'">
                                <span class="sender">{{ msg.sender }}</span>
                                <p class="text">{{ msg.data?.message || msg.message }}</p>
                            </div>
                            <div v-else class="system-text">
                                🔔 {{ msg.type }} 이벤트
                            </div>
                        </div>
                    </div>

                    <div class="chat-input-area">
                        <input 
                            v-model="chatMessage" 
                            @keyup.enter="handleSendChat"
                            type="text" 
                            placeholder="메시지를 입력하세요..." 
                        />
                        <button @click="handleSendChat">전송</button>
                    </div>
                </aside>
            </main>
        </div>
    </div>
</template>

<style scoped>
/* 전체 레이아웃 */
.page-container {
    width: 100vw;
    height: 100vh;
    background-color: #f0f2f5;
    overflow: hidden;
}

/* 로딩 화면 */
.loading-overlay {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100%;
    background: rgba(255, 255, 255, 0.9);
    font-size: 1.2rem;
    color: #555;
}
.error-msg { color: #e74c3c; text-align: center; }
.btn-retry { margin-top: 10px; padding: 8px 16px; background: #e74c3c; color: white; border: none; border-radius: 4px; cursor: pointer; }
.spinner {
    border: 4px solid #f3f3f3;
    border-top: 4px solid #3498db;
    border-radius: 50%;
    width: 40px;
    height: 40px;
    animation: spin 1s linear infinite;
    margin: 0 auto 20px;
}
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }

/* 스터디룸 레이아웃 */
.room-layout {
    display: flex;
    flex-direction: column;
    height: 100%;
}

.room-header {
    height: 60px;
    background: #ffffff;
    border-bottom: 1px solid #ddd;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 20px;
    flex-shrink: 0;
}

.btn-leave {
    background-color: #ff4757;
    color: white;
    border: none;
    padding: 8px 16px;
    border-radius: 4px;
    cursor: pointer;
    font-weight: bold;
}
.btn-leave:hover { background-color: #ff6b81; }

.room-content {
    flex: 1;
    display: flex;
    overflow: hidden;
}

/* 비디오 섹션 */
.video-section {
    flex: 3;
    background-color: #2f3542;
    padding: 20px;
    overflow-y: auto;
}

.video-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 20px;
    justify-content: center;
}

.video-card {
    position: relative;
    background: #000;
    border-radius: 12px;
    overflow: hidden;
    aspect-ratio: 16 / 9;
    box-shadow: 0 4px 10px rgba(0,0,0,0.3);
}

.video-card video {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.video-card .name-tag {
    position: absolute;
    bottom: 10px;
    left: 10px;
    background: rgba(0, 0, 0, 0.6);
    color: white;
    padding: 4px 10px;
    border-radius: 20px;
    font-size: 0.9rem;
}

.video-card.local { border: 2px solid #2ed573; }
.video-card.local video { transform: scaleX(-1); }


/* 채팅 섹션 */
.chat-section {
    flex: 1;
    min-width: 300px;
    max-width: 400px;
    background: white;
    border-left: 1px solid #ddd;
    display: flex;
    flex-direction: column;
}

.chat-header {
    padding: 15px;
    border-bottom: 1px solid #eee;
    background: #f8f9fa;
}

.chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 15px;
    background: #f1f2f6;
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.message-bubble {
    background: white;
    padding: 8px 12px;
    border-radius: 10px;
    max-width: 90%;
    font-size: 0.95rem;
    align-self: flex-start;
    box-shadow: 0 1px 2px rgba(0,0,0,0.1);
}

.message-bubble.my-msg {
    align-self: flex-end;
    background: #d1ccc0; /* 옅은 갈색/베이지 톤 */
    background: #7bed9f; /* 카톡 느낌 원하면 */
}

.message-bubble.sys-msg {
    align-self: center;
    background: none;
    box-shadow: none;
    color: #888;
    font-size: 0.8rem;
}

.sender {
    display: block;
    font-weight: bold;
    font-size: 0.8rem;
    margin-bottom: 4px;
    color: #555;
}

.chat-input-area {
    padding: 15px;
    border-top: 1px solid #ddd;
    display: flex;
    gap: 10px;
    background: white;
}

.chat-input-area input {
    flex: 1;
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 20px;
    outline: none;
}
.chat-input-area button {
    padding: 0 20px;
    border: none;
    border-radius: 20px;
    background: #3742fa;
    color: white;
    cursor: pointer;
}
</style>