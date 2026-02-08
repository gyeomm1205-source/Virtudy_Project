<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useWebcam } from '../logic/useWebcam';
import { avatarAPI } from '../api/avatarAPI';
import { useAuthStore } from '@/stores/authStore'; // 내 정보 스토어
import { useUiStore } from '@/stores/uiStore'; // UI 스토어
import CharacterAvatar from '@/shared/ui/avatar/CharacterAvatar.vue'; // 아바타 컴포넌트
import MatchingModal from '@/shared/ui/MatchingModal.vue';

// 상태 관리
const step = ref<'camera' | 'loading' | 'result'>('camera');
const { videoRef, startCamera, stopCamera, captureImage } = useWebcam();
const authStore = useAuthStore();
const uiStore = useUiStore(); // 스토어 사용
const router = useRouter();


// 생성된 아바타 데이터 임시 저장
const generatedAvatar = ref<any>(null);
const isCapturing = ref(false);
// 촬영 진행 중임을 나타내는 플래그
const isProcessing = ref(false);

// 아바타 생성 횟수 제한 관련 (하루 3번, 날짜별 카운트)
const AVATAR_CREATE_LIMIT = 3;
const AVATAR_CREATE_KEY = 'avatarCreateCountByDate';
const avatarCreateCount = ref<number>(0);
const remainingChances = ref<number>(AVATAR_CREATE_LIMIT);

function getTodayKey() {
  const today = new Date();
  return today.toISOString().slice(0, 10); // YYYY-MM-DD
}

const loadAvatarCreateCount = () => {
  const data = JSON.parse(localStorage.getItem(AVATAR_CREATE_KEY) || '{}');
  const todayKey = getTodayKey();
  avatarCreateCount.value = data[todayKey] || 0;
  remainingChances.value = AVATAR_CREATE_LIMIT - avatarCreateCount.value;
};


const incrementAvatarCreateCount = () => {
  const data = JSON.parse(localStorage.getItem(AVATAR_CREATE_KEY) || '{}');
  const todayKey = getTodayKey();
  data[todayKey] = (data[todayKey] || 0) + 1;
  localStorage.setItem(AVATAR_CREATE_KEY, JSON.stringify(data));
  avatarCreateCount.value = data[todayKey];
  remainingChances.value = AVATAR_CREATE_LIMIT - avatarCreateCount.value;
};

// decrement는 유지하되 로직상 현재는 사용 빈도가 낮음
const decrementAvatarCreateCount = () => {
  const data = JSON.parse(localStorage.getItem(AVATAR_CREATE_KEY) || '{}');
  const todayKey = getTodayKey();
  if (data[todayKey] && data[todayKey] > 0) {
    data[todayKey] = data[todayKey] - 1;
    localStorage.setItem(AVATAR_CREATE_KEY, JSON.stringify(data));
    avatarCreateCount.value = data[todayKey];
    remainingChances.value = AVATAR_CREATE_LIMIT - avatarCreateCount.value;
  }
};

const getChanceMessage = () => {
  return `오늘의 아바타 생성 횟수 ${3 - remainingChances.value}/3`;
};

// 1. 마운트 시 카메라 켜기
onMounted(() => {
  loadAvatarCreateCount();
  if (remainingChances.value > 0) {
    startCamera();
  }
});

// 2. 촬영 및 생성 요청 핸들러

// 저장 여부를 추적하는 플래그
const hasSavedCurrentAvatar = ref(false);

const handleCapture = async () => {
  if (remainingChances.value <= 0 && !isProcessing.value) {
    await uiStore.openAlert(getChanceMessage(), '알림');
    return;
  }

  // 프로세스 시작 플래그 ON (이게 켜져 있으면 횟수가 0이어도 카메라 유지)
  isProcessing.value = true;
  // 촬영 버튼을 누르는 순간 횟수 차감
  incrementAvatarCreateCount(); 
  
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
        await uiStore.openAlert("로그인 정보가 만료되었습니다.\n다시 로그인해주세요.", "인증 만료");
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
    hasSavedCurrentAvatar.value = false; // 새로 촬영하면 저장 안된 상태로 초기화

  } catch (error) {
    console.error('아바타 생성 실패:', error);
    if (error instanceof Error && error.message.includes("유저 정보")) {
      await uiStore.openAlert("내 정보를 불러오는데 실패했습니다.\n새로고침 후 다시 시도해주세요. 😥", "오류");
    } else {
      await uiStore.openAlert("아바타 생성 중 오류가 발생했습니다.", "오류");
    }
    // 에러나면 다시 카메라 화면으로
    step.value = 'camera';
    startCamera();
  }
  finally {
    // 모든 작업이 끝나면 플래그 OFF
    // (성공해서 result로 갔든, 실패해서 camera로 왔든 해제)
    isProcessing.value = false;
  }
};


// 다시 찍기 확인 모달 상태
const showRetryConfirmModal = ref(false);

const handleRetryClick = () => {
  // 바로 리셋하지 않고 확인 모달 띄우기
  showRetryConfirmModal.value = true;
};

const handleRetryConfirm = () => {
  // 모달에서 '네'를 눌렀을 때 실행되는 실제 리셋 로직
  generatedAvatar.value = null;
  step.value = 'camera';
  showRetryConfirmModal.value = false;
  hasSavedCurrentAvatar.value = false;
  
  loadAvatarCreateCount(); // 횟수 정보 갱신
  if (remainingChances.value > 0) {
    startCamera();
  }
};

const handleRetryCancel = () => {
  showRetryConfirmModal.value = false;
};


// 저장 진행 모달 상태
const saveModalState = ref({
  visible: false,
  title: '',
  subtitle: ''
});

const handleConfirm = () => {
  if (!generatedAvatar.value) {
    router.push('/lobby');
    return;
  }

  // 1. 저장 시작 (모달 표시)
  saveModalState.value = {
    visible: true,
    title: '저장 중...',
    subtitle: '잠시만 기다려주세요.'
  };

  // 실제 데이터 저장
  authStore.setAvatarConfig(generatedAvatar.value);
  hasSavedCurrentAvatar.value = true;

  // 2. 1초 뒤 '저장 완료' 메시지로 변경
  setTimeout(() => {
    saveModalState.value = {
      visible: true, // 유지
      title: '저장 완료!',
      subtitle: '이전에 있던 페이지로 돌아갑니다.'
    };

    // 3. 다시 1초 뒤 페이지 이동
    setTimeout(() => {
      saveModalState.value.visible = false;
      router.push('/lobby'); // 이전에 있던 페이지(로비 등)로 이동
    }, 1000);

  }, 1000);
};

</script>

<template>
  <div class="page-container">
    <div v-if="step === 'camera'" class="camera-view">
      <h2 class="title">나만의 아바타 만들기</h2>
      
      <template v-if="remainingChances > 0 || isProcessing">
        <p class="guide-text">정면을 바라보고 촬영 버튼을 눌러주세요!</p>
        <p v-if="remainingChances < AVATAR_CREATE_LIMIT && !isProcessing" class="guide-text" style="color: #b85c00; font-size: 20px; margin-bottom: 0;">
          {{ getChanceMessage() }}
        </p>

        <div class="video-wrapper polaroid-frame" :class="{ 'is-capturing': isCapturing }">
          <div class="flash-overlay" :class="{ 'is-capturing': isCapturing }"></div>
          <video ref="videoRef" autoplay playsinline muted></video>
        </div>

        <button class="capture-btn butter-btn" @click="handleCapture" :disabled="remainingChances <= 0 && !isProcessing">
          <span class="butter-btn-text">촬영</span>
        </button>
      </template>

      <template v-else>
        <p class="guide-text" style="color: #b85c00; font-size: 24px; line-height: 1.5; margin-top: 10px;">
          오늘의 아바타 생성 횟수가 모두 소진되었어요!<br>
          내일 다시 찾아주세요!
        </p>
      </template>



    </div>

    <template v-if="step === 'loading'">
      <Teleport to="body">
        <div class="fixed inset-0 z-[9999]">
          <div class="avatar-loading-overlay"></div>
          <MatchingModal
            :titleText="'아바타 생성 중...'"
            :subtitleText="'AI가 열심히 아바타를 만들고 있어요!'"
            :showCloseButton="false"
            style="z-index: 1000;"
          />
        </div>
      </Teleport>
    </template>

    <div v-else-if="step === 'result'" class="result-view">
      <h2 class="title">🎉짜잔🎉<br>완성되었어요!</h2>
      
      <p class="guide-text" style="color: #b85c00; font-size: 20px; margin-bottom: 0;">
        {{ getChanceMessage() }}
      </p>
      
      <div class="avatar-preview polaroid-frame">
        <div class="avatar-preview-inner">
          <CharacterAvatar 
            v-if="generatedAvatar"
            :config="generatedAvatar"
            :aiDrowsy="0" :aiPhone="0" :aiAbsent="0"
            class="origin-center translate-x-[15%] translate-y-[40%]"
          />
        </div>
      </div>

      <div class="btn-group">
        <button 
          class="retry-btn" 
          @click="handleRetryClick" 
          :disabled="remainingChances <= 0"
          :class="{ 'opacity-50 cursor-not-allowed': remainingChances <= 0 }"
        >
          다시 찍기
        </button>

        <button 
          class="confirm-btn" 
          @click="handleConfirm"
        >
          저장하기
        </button>
      </div>
      <p v-if="remainingChances <= 0" class="mt-2 text-[#b85c00] font-['PfStardust30S'] text-lg">
        마지막 기회였어요! 이 아바타를 저장해주세요. 📸
      </p>
    </div>
    <Teleport to="body">
      <div v-if="showRetryConfirmModal" class="fixed inset-0 bg-[rgba(255,253,245,0.65)] backdrop-blur-md flex items-center justify-center z-50">
        <div class="bg-[var(--color-cream2)] p-[2.5rem] rounded-[0.75rem] shadow-[4px_4px_0px_0px_var(--color-choco)] relative max-w-[26rem] w-full mx-[1rem] flex flex-col items-center gap-[1.5rem] text-center">
          <div>
            <div class="text-[var(--color-choco)] text-[2rem] font-['Xcu'] font-medium leading-none mb-2">
              다시 찍으시겠어요?
            </div>
            <div class="text-[var(--color-syrup)] text-[1.125rem] font-['PfStardust30S'] leading-snug">
              촬영 횟수가 차감돼요!
            </div>
          </div>
          <div class="flex gap-4">
            <button @click="handleRetryConfirm" class="px-6 py-2 border-2 border-[var(--color-choco)] bg-[var(--color-butter)] text-[var(--color-choco)] font-['PfStardust30S'] text-xl rounded shadow-[2px_2px_0px_0px_var(--color-choco)] active:translate-y-1 active:shadow-none transition-all">
              네
            </button>
            <button @click="handleRetryCancel" class="px-6 py-2 border-2 border-[var(--color-choco)] bg-white text-[var(--color-choco)] font-['PfStardust30S'] text-xl rounded shadow-[2px_2px_0px_0px_var(--color-choco)] active:translate-y-1 active:shadow-none transition-all">
              아니오
            </button>
          </div>
        </div>
      </div>
    </Teleport>
    <Teleport to="body">
      <div v-if="saveModalState.visible" class="fixed inset-0 z-[9999]">
        <div class="avatar-loading-overlay"></div>
        <MatchingModal
          :titleText="saveModalState.title"
          :subtitleText="saveModalState.subtitle"
          :showCloseButton="false"
          @close="saveModalState.visible = false"
        />
      </div>
    </Teleport>

  </div>
</template>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100svh;
  height: auto;
  padding: 96px 20px 40px;
  box-sizing: border-box;
  /* 배경 설정 */
  background-image: url('@/assets/bg_brick.png');
  background-repeat: no-repeat;
  background-size: cover;
  background-position: center;
  text-align: center;
  position: relative;
  z-index: 1; /* Ensure page content is below overlays */
}

/* If your logo/menu/header is a separate component, ensure it has a lower z-index, e.g.:
   .header, .logo, .menu { z-index: 10; position: relative; }
   (You may need to update those components/styles separately if not already set.)
*/

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
  background-color: transparent;
}

/* 폴라로이드 공통 스타일 */
.polaroid-frame {
  width: min(300px, 75vw);
  height: auto;
  background-color: white;
  /* 폴라로이드 여백 */
  padding: 12px 12px 60px 12px; 
  /* 그림자 */
  box-shadow: 0 4px 8px rgba(0,0,0,0.2), 0 6px 20px rgba(0,0,0,0.19);
  border-radius: 2px;
  
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 15px 0;
  transition: transform 120ms ease;
  position: relative;
  box-sizing: border-box;
}

.video-wrapper {
  overflow: hidden;
}

video {
  width: 100%;
  aspect-ratio: 1/1;
  object-fit: cover;
  transform: scaleX(-1);
  background: black;
  display: block;
}

.avatar-preview {
  overflow: hidden;
}

/* 아바타 내부 컨테이너 설정 */
.avatar-preview-inner {
  width: 100%;
  aspect-ratio: 1/1;
  display: flex;
  /* 아바타 하단 중앙 정렬 */
  align-items: flex-end; 
  justify-content: center;
  /* 배경 이미지 설정 */
  background-image: url('@/assets/bg_sky_1.png');
  background-size: cover;
  background-position: center bottom;
  background-repeat: no-repeat;
  box-sizing: border-box;
  /* 아바타 컴포넌트 강제 오프셋 초기화 */
  --avatar-offset-x: 0% !important;
  --avatar-offset-y: 0% !important;
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

.title {
  font-size: 50px;
  line-height: 1.2;
  margin-top: 16px;
  font-weight: 700;
  animation: title-wiggle 900ms ease-in-out infinite;
  color: #805143;
}

.guide-text {
  font-size: 30px;
  line-height: 1.4;
  margin-top: -7px;
  color: #805143;
}

.result-view {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  width: 100%;
  max-width: 520px;
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

/* Overlay and modal stacking fix */
.avatar-loading-overlay {
  position: fixed;
  inset: 0;
  /* background: rgba(255, 253, 245, 0.05); */
  /* backdrop-filter: blur(1px); */
  z-index: 2000;
}


.fixed.inset-0.bg-\[rgba\(255\,253\,245\,0\.65\)\].backdrop-blur-md {
  z-index: 2100 !important;
}

.MatchingModal,
.matching-modal,
.modal {
  z-index: 2200 !important;
  position: relative;
}
</style>