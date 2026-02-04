<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useWebcam } from '../logic/useWebcam';
import { avatarAPI } from '../api/avatarAPI';
import { useAuthStore } from '@/stores/authStore'; // 내 정보 스토어
import CharacterAvatar from '@/shared/ui/avatar/CharacterAvatar.vue'; // 아바타 컴포넌트

// 상태 관리
const step = ref<'camera' | 'loading' | 'result'>('camera');
const { videoRef, startCamera, stopCamera, captureImage } = useWebcam();
const authStore = useAuthStore();
const router = useRouter();

// 생성된 아바타 데이터 임시 저장
const generatedAvatar = ref<any>(null);

// 1. 마운트 시 카메라 켜기
onMounted(() => {
  startCamera();
});

// 2. 촬영 및 생성 요청 핸들러
const handleCapture = async () => {
  const file = await captureImage();
  if (!file) return;

  // 로딩 화면으로 전환
  stopCamera(); // 카메라는 끔
  step.value = 'loading';

  try {
    // 스토어에 유저 정보가 없다면 다시 불러온다
    if (!authStore.userInfo) {

      // 로그인이 안 되었다면 걸러낸다
      if (!authStore.accessToken) {
        alert("로그인 정보가 만료되었습니다. 다시 로그인해주세요.");
        router.push('/guest'); // 로그인 페이지로 쫓아내기
        return;
      }

      await authStore.fetchUserInfo();
    }

    if (!authStore.userInfo) {
      throw new Error("유저 정보를 불러올 수 없습니다."); // 여기서 강제로 에러로 보냄
    }

    // 토큰이 있는지 먼저 확인

    const nickname = authStore.userInfo.nickName;

    if (!nickname) {
      throw new Error("닉네임 정보가 없습니다.");
    }

    // 아바타 생성 API 호출
    const newConfig = await avatarAPI.generateAvatar(file, nickname);

    // 결과 저장 및 화면 전환
    generatedAvatar.value = newConfig;
    
    step.value = 'result';

  } catch (error) {
    console.error('아바타 생성 실패:', error);

    // 디버깅용
    if (error instanceof Error && error.message.includes("유저 정보")) {
        alert("내 정보를 불러오는데 실패했습니다. 새로고침 후 다시 시도해주세요. 😥");
    } else {
        alert("아바타 생성 중 오류가 발생했습니다.");
    }
  }
};

const handleRetry = () => {
  generatedAvatar.value = null;
  step.value = 'camera';
  startCamera();
};

const handleConfirm = () => {
  if (generatedAvatar.value) {
    authStore.setAvatarConfig(generatedAvatar.value);
  }
  router.push('/lobby'); // 로비나 마이페이지로 이동
};
</script>

<template>
  <div class="page-container">
    
    <div v-if="step === 'camera'" class="camera-view">
      <h2 class="title">나만의 아바타 만들기</h2>
      <div class="video-wrapper">
        <video ref="videoRef" autoplay playsinline muted></video>
      </div>
      <p class="guide-text">정면을 바라보고 버튼을 눌러주세요!</p>
      <button class="capture-btn" @click="handleCapture">
        📸 찰칵!
      </button>
    </div>

    <div v-else-if="step === 'loading'" class="loading-view">
      <div class="spinner"></div> <h3>AI가 열심히 아바타를<br>만들고 있어요! 🎨</h3>
      <p>잠시만 기다려주세요...</p>
    </div>

    <div v-else-if="step === 'result'" class="result-view">
      <h2 class="title">짜잔! 완성되었어요 🎉</h2>
      
      <div class="avatar-preview">
        <CharacterAvatar 
          v-if="generatedAvatar"
          :config="generatedAvatar"
          :aiDrowsy="0" :aiPhone="0" :aiAbsent="0"
        />
      </div>

      <div class="btn-group">
        <button class="retry-btn" @click="handleRetry">
          다시 찍기
        </button>
        <button class="confirm-btn" @click="handleConfirm">
          저장하기
        </button>
      </div>
    </div>

  </div>
</template>

<style scoped>
/* 간단한 스타일 예시 */
.page-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background-color: var(--color-bg); /* 테마 컬러 */
  text-align: center;
}

.result-view {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.avatar-preview {
  width: 260px;
  height: 260px;
  margin: 16px 0;
}

.btn-group {
  display: flex;
  gap: 12px;
  z-index: 1;
}

.retry-btn,
.confirm-btn {
  padding: 10px 18px;
  border: 2px solid var(--color-choco);
  border-radius: 8px;
  background: var(--color-butter);
  color: var(--color-choco);
  font-family: 'PfStardust30S', sans-serif;
  cursor: pointer;
}

.video-wrapper {
  width: 320px;
  height: 320px;
  border-radius: 50%; /* 원형 카메라 */
  overflow: hidden;
  border: 5px solid var(--color-primary);
  margin: 20px 0;
  background: black;
}

video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform: scaleX(-1); /* 거울 모드 */
}

/* 로딩 스피너 애니메이션 */
.spinner {
  width: 50px;
  height: 50px;
  border: 5px solid #f3f3f3;
  border-top: 5px solid var(--color-primary);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 버튼 스타일 생략... */
</style>