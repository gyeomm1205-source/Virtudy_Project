<script setup lang="ts">
import { ref } from 'vue';
import { useStudyRoom } from '../composables/useStudyRoom';

const roomIdInput = ref('room-1');
const memberIdInput = ref('member-' + Math.floor(Math.random() * 1000));
const chatMessage = ref('');

const { joinRoom, leaveRoom, sendChat, isConnected, error, messages } = useStudyRoom();

// 방 입장 핸들러
const handleJoin = async () => {
    if (!roomIdInput.value || !memberIdInput.value) {
        alert('방 번호와 멤버 ID를 입력해주세요.');
        return;
    }
    await joinRoom(roomIdInput.value, memberIdInput.value);
};

// 방 퇴장 핸들러
const handleLeave = () => {
    leaveRoom();
};

// 채팅 전송 핸들러
const handleSendChat = () => {
    if (!chatMessage.value) return;
    sendChat(chatMessage.value);
    chatMessage.value = ''; // 입력창 초기화
};
</script>

<template>
    <div class="room-container">
        <h1>스터디룸 데모 (Dual Connection)</h1>

        <!-- 연결 전: 입장 폼 -->
        <div v-if="!isConnected" class="login-box">
            <div class="input-group">
                <label>방 번호:</label>
                <input v-model="roomIdInput" type="text" placeholder="예: study-101" />
            </div>
            <div class="input-group">
                <label>내 아이디:</label>
                <input v-model="memberIdInput" type="text" placeholder="예: user-1" />
            </div>
            <button @click="handleJoin" class="btn primary">입장하기</button>
            <p v-if="error" class="error-msg">{{ error }}</p>
        </div>

        <!-- 연결 후: 방 화면 -->
        <div v-else class="room-view">
            <div class="header">
                <h2>Room: {{ roomIdInput }}</h2>
                <button @click="handleLeave" class="btn danger">나가기</button>
            </div>

            <div class="grid-layout">
                <!-- 왼쪽: 비디오 그리드 (LiveKit) -->
                <div class="video-section">
                    <h3>📹 비디오 그리드</h3>
                    <div id="grid-container" class="video-grid">
                        <!-- 여기에 LiveKit 비디오 엘리먼트들이 추가됩니다 (추후 구현) -->
                        <div class="placeholder">내 카메라 화면 (준비중)</div>
                    </div>
                </div>

                <!-- 오른쪽: 제어/채팅 패널 (WebSocket) -->
                <div class="control-section">
                    <h3>💬 채팅 및 제어</h3>
                    <div class="chat-box">
                        <div class="messages">
                            <p class="sys-msg">채팅방에 입장했습니다.</p>
                            <div v-for="(msg, index) in messages" :key="index" class="msg-item">
                                <span v-if="msg.type === 'CHAT'">
                                    <strong>{{ msg.sender === 'me' ? '나' : msg.sender }}:</strong> {{ msg.message }}
                                </span>
                                <span v-else class="sys-msg">
                                    {{ msg.type }} 이벤트 발생
                                </span>
                            </div>
                        </div>
                        <div class="chat-input">
                            <input 
                                v-model="chatMessage" 
                                @keyup.enter="handleSendChat"
                                type="text" 
                                placeholder="메시지를 입력하세요..." 
                            />
                            <button @click="handleSendChat">전송</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.room-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
    font-family: 'Helvetica Neue', Arial, sans-serif;
}

.login-box {
    max-width: 400px;
    margin: 50px auto;
    padding: 30px;
    border: 1px solid #ddd;
    border-radius: 8px;
    box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.input-group {
    margin-bottom: 15px;
    text-align: left;
}

.input-group label {
    display: block;
    margin-bottom: 5px;
    font-weight: bold;
}

.input-group input {
    width: 100%;
    padding: 8px;
    border: 1px solid #ccc;
    border-radius: 4px;
}

.btn {
    padding: 10px 20px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-weight: bold;
}

.btn.primary { background-color: #4CAF50; color: white; width: 100%; }
.btn.danger { background-color: #f44336; color: white; }

.error-msg { color: red; margin-top: 10px; }

.room-view { margin-top: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }

.grid-layout {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 20px;
    height: 600px;
}

.video-section { background: #2c3e50; color: white; border-radius: 8px; padding: 10px; }
.video-grid { 
    display: grid; 
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); 
    gap: 10px; 
    margin-top: 10px;
}
.placeholder {
    background: #34495e;
    height: 150px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 4px;
}

.control-section { background: #f8f9fa; border-radius: 8px; padding: 10px; display: flex; flex-direction: column; }
.chat-box { flex: 1; display: flex; flex-direction: column; }
.messages { flex: 1; border: 1px solid #e9ecef; border-radius: 4px; margin-bottom: 10px; padding: 10px; background: white; overflow-y: auto; }
.chat-input { display: flex; gap: 5px; }
.chat-input input { flex: 1; padding: 8px; }
</style>
