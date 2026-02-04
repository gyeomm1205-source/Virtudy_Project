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
const isCapturing = ref(false);

// 1. 마운트 시 카메라 켜기
onMounted(() => {
  startCamera();
});

// 2. 촬영 및 생성 요청 핸들러
const handleCapture = async () => {
  isCapturing.value = true;
  window.setTimeout(() => {
    isCapturing.value = false;
  }, 220);

  // 짧게 촬영 애니메이션을 보여준 뒤 캡처 진행
  await new Promise((resolve) => window.setTimeout(resolve, 180));

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
  <div class="page-container bg-[var(--color-cream2)]">
    
    <div v-if="step === 'camera'" class="camera-view bg-[var(--color-cream)]">
      <h2 class="title">나만의 아바타 만들기</h2>
      <p class="guide-text">정면을 바라보고 촬영 버튼을 눌러주세요!</p>
      <div class="video-wrapper" :class="{ 'is-capturing': isCapturing }">
        <div class="flash-overlay" :class="{ 'is-capturing': isCapturing }"></div>
        <video ref="videoRef" autoplay playsinline muted></video>
      </div>
      <button class="capture-btn butter-btn" @click="handleCapture">
        <span class="butter-btn-text">촬영</span>
      </button>
    </div>

    <div v-else-if="step === 'loading'" class="loading-view">
      <div class="spinner"></div> <h3>AI가 열심히 아바타를<br>만들고 있어요! 🎨</h3>
      <p>잠시만 기다려주세요...</p>
    </div>

    <div v-else-if="step === 'result'" class="result-view">
      <h2 class="title">🎉짜잔🎉<br>완성되었어요!</h2>
      
      <div class="avatar-preview">
        <div
          class="avatar-preview-inner"
          :style="{ transform: 'translate(31px, 56px)' }"
        >
          <CharacterAvatar 
            v-if="generatedAvatar"
            :config="generatedAvatar"
            :aiDrowsy="0" :aiPhone="0" :aiAbsent="0"
          />
        </div>
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
  min-height: 100svh;
  height: auto;
  padding: 96px 20px 40px;
  box-sizing: border-box;
  background-color: transparent; /* 테마 컬러 */
  text-align: center;
}

.camera-view {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: 520px;
  gap: 12px;
  padding: 20px 16px 28px;
  box-sizing: border-box;
  border-radius: 16px;
}

.title {
  font-size: 50px;
  line-height: 1.2;
  margin-top: 16px;
  font-weight: 700;
  animation: title-wiggle 900ms ease-in-out infinite;
}

.guide-text {
  font-size: 30px;
  line-height: 1.4;
  margin-top: -7px;
}

.loading-view h3 {
  font-size: clamp(27px, 4vw, 35px);
  line-height: 1.3;
  font-weight: 700;
}

.loading-view p {
  font-size: clamp(16px, 3.2vw, 20px);
}


.result-view {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  width: 100%;
  max-width: 520px;
}

.avatar-preview {
  width: min(260px, 70vw);
  height: min(260px, 70vw);
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #ffffff;
  border: 3px solid var(--color-choco);
}

.avatar-preview-inner {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 120ms ease;
}


.btn-group {
  display: flex;
  gap: 27px;
  z-index: 1;
  flex-wrap: wrap;
  justify-content: center;
  margin-top: 10px;
}

.retry-btn,
.confirm-btn {
  padding: 10px 18px;
  border: 2px solid var(--color-choco);
  border-radius: 8px;
  background: var(--color-butter);
  color: var(--color-choco);
  font-family: 'PfStardust30S', sans-serif;
  font-size: 20px;
  cursor: pointer;
  transition: transform 140ms ease, box-shadow 140ms ease, opacity 140ms ease;
  box-shadow: 2px 2px 0px 0px var(--color-choco);
}

.retry-btn:hover,
.confirm-btn:hover {
  transform: translateY(-2px);
  box-shadow: 4px 4px 0px 0px var(--color-choco);
}

.retry-btn:active,
.confirm-btn:active {
  transform: translateY(0);
  box-shadow: 1px 1px 0px 0px var(--color-choco);
  opacity: 0.9;
}

.capture-btn {
  margin: 5px auto 0;
  padding: 16px 32px;
}

.capture-btn .butter-btn-text {
  font-size: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  height: 1em;
  vertical-align: middle;
  transform: translateY(-2px);
  font-weight: 700;
}

.video-wrapper {
  width: min(320px, 80vw);
  height: min(320px, 80vw);
  border-radius: 50%;
  overflow: hidden;
  border: 5px solid var(--color-primary);
  margin: 15px 0;
  background: black;
  position: relative;
  transition: transform 120ms ease;
}

.flash-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.9);
  opacity: 0;
  pointer-events: none;
  z-index: 2;
  mix-blend-mode: screen;
}

.flash-overlay.is-capturing {
  animation: flash 160ms ease-out;
}

video {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform: scaleX(-1); /* 거울 모드 */
  position: relative;
  z-index: 1;
}

/* 로딩 스피너 애니메이션 */
.spinner {
  width: 80px;
  height: 80px;
  border: 5px solid #f3f3f3;
  border-top: 5px solid var(--color-syrup);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes flash {
  0% { opacity: 0; }
  10% { opacity: 1; }
  60% { opacity: 0.6; }
  100% { opacity: 0; }
}

@keyframes title-wiggle {
  0% { transform: rotate(0deg); }
  25% { transform: rotate(-1.5deg); }
  50% { transform: rotate(1.5deg); }
  75% { transform: rotate(-1deg); }
  100% { transform: rotate(0deg); }
}


/* 버튼 스타일 생략... */
</style>
