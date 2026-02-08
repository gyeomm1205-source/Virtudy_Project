<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

// 페이지 컴포넌트
import UserPage from '@/features/main/pages/UserPage.vue';
import LobbyPage from '@/features/lobby/pages/LobbyPage.vue';
import RankingPage from '@/features/ranking/pages/RankingPage.vue';
import MyPage from '@/features/mypage/pages/MyPage.vue';
import ReportPage from '@/features/report/pages/ReportPage.vue';
import GlobalBackground from '@/shared/ui/GlobalBackground.vue';
import CharacterAvatar from '@/shared/ui/avatar/CharacterAvatar.vue';
import MockStudyRoom from './MockStudyRoom.vue';

const router = useRouter();
const authStore = useAuthStore();

// --- 1. Step 정의 ---
type TutorialStep = {
  text: string;
  bgComponent: any; // 'AvatarCamera' | 'AvatarResult' | Component
  targetSelector?: string; 
  messagePos: { top: string; left: string };
  onEnter?: () => void;
  // MockStudyRoom용 상태 Props
  mockProps?: {
    bgState?: 'GREEN' | 'YELLOW' | 'RED';
    avatarState?: 'FOCUS' | 'SLEEP' | 'PHONE' | 'AWAY';
    showWakeUpModal?: boolean;
  };
};

const steps: TutorialStep[] = [
  // 인트로
  {
    text: "어서 오세요!\nVirtudy버터디에 대해 알아볼까요?",
    bgComponent: null, // Intro Background
    messagePos: { top: '50%', left: '50%' },
  },

    // --- 0. 메인 페이지 ---
  {
    text: "이곳은\n메인 페이지입니다.",
    bgComponent: UserPage,
    messagePos: { top: '50%', left: '50%' },
  },
    {
    text: "아바타 생성하기 버튼을\n눌러 보세요!",
    bgComponent: UserPage,
    targetSelector: '.profile-image-container', // 프로필 사진 클릭하도록 유도
    messagePos: { top: '50%', left: '50%' },
  },
  
  // --- 1. 아바타 생성 (Mock) ---
  {
    text: "아바타 생성 화면입니다.",
    bgComponent: 'AvatarCamera',
    messagePos: { top: '30%', left: '50%' },
  },
  {
    text: "웹캠으로 사진을 찍으면\nAI가 나와 닮은 아바타 파츠를 매칭해 줘요.\n촬영 버튼을 눌러 보세요.",
    bgComponent: 'AvatarCamera',
    targetSelector: '.capture-btn', // 촬영 버튼 강조
    messagePos: { top: '30%', left: '50%' },
  },
  {
    text: "하루에 3번까지\n아바타를 재생성해 볼 수 있습니다.",
    bgComponent: 'AvatarResult', // 결과 화면
    messagePos: { top: '30%', left: '50%' },
  },

  // --- 2. 메인 페이지 ---
  {
    text: "다시 메인 페이지로 \n돌아옵니다.",
    bgComponent: UserPage,
    messagePos: { top: '50%', left: '50%' },
    onEnter: () => {
      tutorialLobbyTab.value = 'all'; // 초기화
    }
  },
  {
    text: "랜덤매칭을 통해\n나와 학습 성향이 비슷한 사람이 모여있는\n스터디룸에 자동으로 입장할 수 있어요!",
    bgComponent: UserPage,
    targetSelector: '.random-match-btn', // 랜덤 매칭 버튼
    messagePos: { top: '65%', left: '30%' },
  },
  {
    text: "방 만들기를 통해\n직접 스터디룸을 생성할 수 있습니다.\n한 사람당 3개까지 스터디룸을 만들 수 있어요!",
    bgComponent: UserPage,
    targetSelector: '.create-room-btn', // 방 만들기 버튼
    messagePos: { top: '65%', left: '50%' },
  },
  {
    text: "전체 목록을 클릭해\n방(스터디룸) 목록을 볼 수 있습니다.",
    bgComponent: UserPage,
    targetSelector: '.room-list-btn', // 방 목록 버튼
    messagePos: { top: '75%', left: '50%' },
  },

  // --- 3. 방 목록 (LobbyPage) ---
  // 2-1. 전체 목록
  {
    text: "전체 목록에서는\n모든 방을 볼 수 있고,\n입장할 수 있습니다.",
    bgComponent: LobbyPage,
    targetSelector: '.lobby-roomlist', 
    messagePos: { top: '50%', left: '20%' },
    onEnter: () => clickLobbyTab(0), // '전체' 탭 클릭
  },
  // 2-2. 내 방 목록
  {
    text: "내 방 목록에서는\n내가 만든 방, '최애 스터디'로 지정한 방,\n내가 최근 입장했던 방들을 볼 수 있습니다.",
    bgComponent: LobbyPage,
    targetSelector: '.lobby-roomlist',
    messagePos: { top: '50%', left: '20%' },
    onEnter: () => clickLobbyTab(1), // '내 방' 탭 클릭
  },
  // 2-3. 최애 스터디 소개
  {
    text: "최애 스터디 기능을\n알아볼까요?",
    bgComponent: LobbyPage,
    messagePos: { top: '50%', left: '50%' },
  },
  {
    text: "최애 스터디 버튼을 누르면\n하트 UI가 활성화되면서,\n최애 스터디를 설정하거나 바꿀 수 있습니다.",
    bgComponent: LobbyPage,
    targetSelector: '.favorite-select-btn', // 최애방 선택하기 버튼만 강조
    messagePos: { top: '30%', left: '50%' },
  },
  {
    text: "설정한 최애 스터디는\n내 방 목록과 프로필에서도 확인할 수 있고,\n'팀 랭킹'에서 '내 팀'으로 표시됩니다.",
    bgComponent: LobbyPage,
    targetSelector: '.lobby-roomlist', // 방 목록 섹션 강조
    messagePos: { top: '50%', left: '20%' },
  },
  
  // --- 4. 방 입장 전 ---
  {
    text: "이제 방으로 들어가 볼까요?",
    bgComponent: LobbyPage,
    targetSelector: '.first-room-card', // 첫번째 방만 강조
    messagePos: { top: '55%', left: '50%' },
    onEnter: () => {
      tutorialLobbyTab.value = 'all'; // 다시 전체 탭으로
      setTimeout(updateSpotlightPosition, 200);
    }
  },

  // --- 5. 스터디룸 내부 (MockStudyRoom) ---
  {
    text: "스터디룸에 어서 오세요!",
    bgComponent: MockStudyRoom,
    messagePos: { top: '50%', left: '50%' },
  },
  {
    text: "방에 들어가면 내 캐릭터와 다른 유저의 캐릭터를 같이 볼 수 있습니다.",
    bgComponent: MockStudyRoom,
    targetSelector: '.tutorial-target-avatar',
    messagePos: { top: '60%', left: '50%' },
    mockProps: { avatarState: 'FOCUS' }
  },
  // 캐릭터 상태 변화
  {
    text: "캐릭터는 '집중'...",
    bgComponent: MockStudyRoom,
    targetSelector: '.tutorial-target-friend', // 친구 아바타만 강조
    messagePos: { top: '75%', left: '30%' },
    mockProps: { avatarState: 'FOCUS' }
  },
  {
    text: "...'졸음'...",
    bgComponent: MockStudyRoom,
    targetSelector: '.tutorial-target-friend',
    messagePos: { top: '75%', left: '30%' },
    mockProps: { avatarState: 'SLEEP' }
  },
  {
    text: "...'핸드폰 사용'...",
    bgComponent: MockStudyRoom,
    targetSelector: '.tutorial-target-friend',
    messagePos: { top: '75%', left: '30%' },
    mockProps: { avatarState: 'PHONE' }
  },
  {
    text: "...'자리 비움'에 따라\n상태가 변화합니다.",
    bgComponent: MockStudyRoom,
    targetSelector: '.tutorial-target-friend',
    messagePos: { top: '75%', left: '30%' },
    mockProps: { avatarState: 'AWAY' }
  },
  {
    text: "내 집중 정도는 이름 옆의 하트로 확인할 수 있습니다.'집중'상태를 유지하지 않으면 \n하트 색이 초록, 노랑, 빨강 순으로 변화합니다.",
    bgComponent: MockStudyRoom,
    targetSelector: '.avatar-card.local .user-info',
    messagePos: { top: '80%', left: '50%' },
    mockProps: { avatarState: 'FOCUS' } // 리셋
  },
  // 배경 변화
  {
    text: "창문 밖에는\n버터 마을의 풍경이 비칩니다.",
    bgComponent: MockStudyRoom,
    targetSelector: '.tutorial-target-window',
    messagePos: { top: '50%', left: '75%' },
    mockProps: { bgState: 'GREEN' }
  },
  {
    text: "스터디룸 내 사람들이\n모두 집중하고 있을 때는\n평화롭지만,",
    bgComponent: MockStudyRoom,
    targetSelector: '.tutorial-target-window',
    messagePos: { top: '50%', left: '75%' },
    mockProps: { bgState: 'GREEN' }
  },
  {
    text: "집중하지 않는 사람들이 늘어나면\n멸망하기 시작합니다.",
    bgComponent: MockStudyRoom,
    targetSelector: '.tutorial-target-window',
    messagePos: { top: '50%', left: '75%' },
    mockProps: { bgState: 'YELLOW' }
  },
  {
    text: "마지막에는\n결국 멸망한 마을이 보이게 됩니다.",
    bgComponent: MockStudyRoom,
    targetSelector: '.tutorial-target-window',
    messagePos: { top: '50%', left: '75%' },
    mockProps: { bgState: 'RED' }
  },
  // 타이머
  {
    text: "집중 타이머와\n전체 타이머가 있습니다.",
    bgComponent: MockStudyRoom,
    targetSelector: '.timer-display',
    messagePos: { top: '30%', left: '50%' },
    mockProps: { bgState: 'GREEN' } // 리셋
  },
  {
    text: "집중 타이머는\n내가 집중하고 있는 시간\n동안에만 흐릅니다.",
    bgComponent: MockStudyRoom,
    targetSelector: '.focus-timer-area',
    messagePos: { top: '30%', left: '50%' },
  },
  {
    text: "전체 타이머는\n내가 방에 들어온 시간부터\n계속해서 흘러갑니다.",
    bgComponent: MockStudyRoom,
    targetSelector: '.total-timer-area',
    messagePos: { top: '30%', left: '50%' },
  },
  // 깨우기
  {
    text: "다른 유저가 '노란 하트'나 '빨간 하트'일 때,\n깨우기 버튼이 활성화됩니다.",
    bgComponent: MockStudyRoom,
    targetSelector: '.tutorial-target-wakeup',
    messagePos: { top: '40%', left: '60%' },
    mockProps: { bgState: 'YELLOW', avatarState: 'SLEEP' } 
  },
  {
    text: "깨우기 버튼을 누르면\n집중하지 않는 유저를 골라\n섬광탄을 보낼 수 있습니다.",
    bgComponent: MockStudyRoom,
    targetSelector: '.tutorial-target-wakeup-modal',
    messagePos: { top: '50%', left: '75%' },
    mockProps: { bgState: 'YELLOW', avatarState: 'SLEEP', showWakeUpModal: true } // 모달 띄움
  },
  {
    text: "물론 내가 맞을 수도 있습니다!\n매우 눈이 아프니 주의하세요!",
    bgComponent: MockStudyRoom,
    targetSelector: '.tutorial-target-wakeup-modal',
    messagePos: { top: '50%', left: '75%' },
    mockProps: { showWakeUpModal: true }
  },
  {
    text: "PIP 기능을 이용하면\n다른 화면을 켠 상태에서도 \n나와 방의 상태를 확인할 수 있습니다.\n버터 마을에서 공부하는 기분을 \n작은 화면에서도 즐겨 보세요.",
    bgComponent: MockStudyRoom,
    targetSelector: '.tutorial-target-pip',
    messagePos: { top: '30%', left: '60%' },
    mockProps: { showWakeUpModal: false } // 모달 닫음
  },

  // --- 6. 랭킹 ---
  {
    text: "집중 시간을 유지하면 유지할수록 \n점수가 쌓이고, \n랭킹이 오릅니다.",
    bgComponent: RankingPage,
    messagePos: { top: '50%', left: '50%' },
  },
  {
    text: "개인에게는 티어가 부여됩니다.\n티어는 절대적이지만\n순위는 상대적입니다.",
    bgComponent: RankingPage,
    targetSelector: '.rank-table', 
    messagePos: { top: '50%', left: '20%' },
  },
  {
    text: "티어는\n브론즈, 실버, 골드, 플래티넘, 다이아 순입니다.\n다이아 티어를 노려 보세요!",
    bgComponent: RankingPage,
    messagePos: { top: '40%', left: '60%' },
  },

  // --- 7. 마이페이지 (간단 언급) ---
  {
    text: "마이페이지에서는 \n내 프로필과 리포트를 \n확인할 수 있습니다.",
    bgComponent: MyPage,
    targetSelector: '.mypage-menu',
    messagePos: { top: '50%', left: '50%' },
  },
  {
    text: "리포트에서는 \n주간 단위로 리포트를 볼 수 있어요!\n일주일간 열심히 공부하면 \n그 다음 주 월요일에 리포트가 갱신됩니다.",
    bgComponent: ReportPage,
    targetSelector: '.report-content',
    messagePos: { top: '50%', left: '20%' },
  },
];

// --- 2. 상태 및 로직 ---
const currentStepIndex = ref(0);
const currentStep = computed(() => steps[currentStepIndex.value]);
const spotlightStyle = ref({});
const isSkipModalOpen = ref(false);
const isTutorialFinished = ref(false);
// 튜토리얼 탭 상태 관리
const tutorialLobbyTab = ref<'all' | 'myRooms'>('all');

// 더미 아바타 (결과 화면용)
const dummyAvatarConfig = {
  hairFront: 'hair_f_1', hairBack: 'hair_b_1', hairColor: '#7D6F64',
  eyes: 'eyes_1', glasses: 'glasses_1', outfit: 'outfit_1', clothesColor: '#FFFFFF'
};

// 탭 클릭 시뮬레이션
const clickLobbyTab = (index: number) => {
  nextTick(() => {
    // 튜토리얼 모드에서는 직접 상태 변경
    tutorialLobbyTab.value = index === 0 ? 'all' : 'myRooms';
    // DOM 업데이트 후 스포트라이트 위치 업데이트
    setTimeout(updateSpotlightPosition, 100);
  });
};

// 스포트라이트 위치 업데이트
const updateSpotlightPosition = () => {
  const step = currentStep.value;
  if (!step.targetSelector) {
    spotlightStyle.value = {}; 
    return;
  }

  const target = document.querySelector(step.targetSelector);
  if (target) {
    const rect = target.getBoundingClientRect();
    const padding = 10; 
    
    // 아바타 타겟인 경우 위쪽으로 200px 확장
    const isAvatarTarget = step.targetSelector === '.tutorial-target-avatar' || step.targetSelector === '.tutorial-target-friend';
    const topExtension = isAvatarTarget ? 200 : 0;
    const heightExtension = isAvatarTarget ? 200 : 0;
    
    spotlightStyle.value = {
      top: `${rect.top - padding - topExtension}px`,
      left: `${rect.left - padding}px`,
      width: `${rect.width + (padding * 2)}px`,
      height: `${rect.height + (padding * 2) + heightExtension}px`,
      borderRadius: '12px',
      // box-shadow 트릭으로 주변만 어둡게
      boxShadow: '0 0 0 9999px rgba(0, 0, 0, 0.6)', 
      position: 'absolute',
      zIndex: 999,
      pointerEvents: 'none', // 버튼 클릭 방지
      transition: 'all 0.3s ease'
    };
  } else {
    // 타겟 못 찾음 -> 전체 어둡게 (Fallback)
    spotlightStyle.value = {};
  }
};

let resizeObserver: ResizeObserver | null = null;

onMounted(async () => {
  // 더미 유저 세팅 (튜토리얼용)
  if (!authStore.userInfo) {
    authStore.setUserInfo({
      userId: "tutorial", nickName: "튜토리얼", email: "tutorial@test.com",
      jobType: "DEV", tier: "GOLD", tierScore: 100, 
      avatar: dummyAvatarConfig,
      avatarImageUrl: "", dailyPureStudyTime: 0, dailyFocusDepth: 0, favoriteRoomTitle: ""
    });
  }

  resizeObserver = new ResizeObserver(() => updateSpotlightPosition());
  resizeObserver.observe(document.body);

  await nextTick();
  updateSpotlightPosition();
});

onUnmounted(() => {
  if (resizeObserver) resizeObserver.disconnect();
});

watch(currentStepIndex, async () => {
  await nextTick();
  if (currentStep.value.onEnter) {
    currentStep.value.onEnter();
    setTimeout(updateSpotlightPosition, 200); // 탭 전환 등 DOM 변경 대기
  } else {
    updateSpotlightPosition();
  }
});

const handleNext = () => {
  if (isSkipModalOpen.value) return;
  if (currentStepIndex.value < steps.length - 1) {
    currentStepIndex.value++;
  } else {
    isTutorialFinished.value = true;
  }
};

// 튜토리얼 종료/스킵 시 유저 페이지로 이동
const finishTutorial = () => {
  router.push('/');
};

</script>

<template>
  <div class="tutorial-root" @click="handleNext">
    
    <div class="background-layer">
      <GlobalBackground :skyType="1">
        
        <div v-if="currentStep.bgComponent === 'AvatarCamera'" class="w-full h-full flex items-center justify-center p-4">
          <div class="polaroid-frame bg-white p-4 pb-12 shadow-xl rotate-1">
            <div class="w-[300px] h-[300px] bg-black relative flex items-center justify-center overflow-hidden">
                <span class="text-white">Webcam Preview</span>
                <div class="absolute inset-0 border-4 border-dashed border-white/50 m-8"></div>
            </div>
            <button class="capture-btn butter-btn mt-4 mx-auto block">
                <span class="butter-btn-text">촬영</span>
            </button>
          </div>
        </div>

        <div v-else-if="currentStep.bgComponent === 'AvatarResult'" class="w-full h-full flex items-center justify-center p-4">
          <div class="flex flex-col items-center gap-4">
            <h2 class="text-[#805143] text-4xl font-['Ram'] mb-2 animate-bounce">🎉짜잔🎉<br>완성되었어요!</h2>
            <div class="polaroid-frame bg-white p-4 pb-12 shadow-xl -rotate-1">
                <div class="w-[300px] h-[300px] relative bg-[url('@/assets/bg_sky_1.png')] bg-cover bg-bottom flex items-end justify-center">
                  <CharacterAvatar :config="dummyAvatarConfig" :aiDrowsy="0" :aiPhone="0" :aiAbsent="0" />
                </div>
            </div>
            <div class="flex gap-4">
                <button class="bg-[var(--color-butter)] border-2 border-[var(--color-choco)] text-[var(--color-choco)] px-4 py-2 font-['PfStardust30S']">다시 찍기</button>
                <button class="bg-[var(--color-butter)] border-2 border-[var(--color-choco)] text-[var(--color-choco)] px-4 py-2 font-['PfStardust30S']">저장하기</button>
            </div>
          </div>
        </div>

        <MockStudyRoom 
          v-else-if="currentStep.bgComponent === MockStudyRoom"
          v-bind="currentStep.mockProps"
        />

        <component 
          v-else-if="currentStep.bgComponent" 
          :is="currentStep.bgComponent"
          class="origin-top"
          :tutorialMode="true"
          :tutorialTab="tutorialLobbyTab"
        />

      </GlobalBackground>
    </div>

    <template v-if="!isTutorialFinished">
      <div 
        v-if="currentStep.targetSelector" 
        class="spotlight-overlay"
        :style="spotlightStyle"
      ></div>
      
      <div v-else class="full-overlay"></div>

      <div 
        class="message-box"
        :style="{ top: currentStep.messagePos.top, left: currentStep.messagePos.left }"
      >
        <p>{{ currentStep.text }}</p>
      </div>

      <button @click.stop="isSkipModalOpen = true" class="btn-skip">SKIP</button>
    </template>

    <div v-if="isTutorialFinished" class="finish-overlay">
      <div class="finish-content flex flex-col items-center">
          <p class="text-[var(--color-butter)] text-4xl font-['Ram'] mb-8 text-center leading-relaxed drop-shadow-md">
            이제 튜토리얼을 마쳤어요!<br>바로 공부하러 가 볼까요?
          </p>
          <button @click.stop="finishTutorial" class="btn-finish">네!</button>
      </div>
    </div>

    <div v-if="isSkipModalOpen" class="fixed inset-0 bg-[rgba(255,253,245,0.65)] backdrop-blur-md flex items-center justify-center" style="z-index: 3005;" @click.stop>
      <div class="bg-[var(--color-cream2)] p-[2.5rem] rounded-[0.75rem] shadow-[4px_4px_0px_0px_var(--color-choco)] relative max-w-[26rem] w-full mx-[1rem]">
        <button 
          @click="isSkipModalOpen = false"
          class="absolute top-[1rem] right-[1rem] w-[2.625rem] h-[2.625rem] cursor-pointer hover:scale-110 transition-transform flex items-center justify-center"
        >
          <div class="w-[1.5rem] h-[1.5rem] relative">
            <div class="absolute top-[4px] left-[4px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
            <div class="absolute top-[8px] left-[8px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
            <div class="absolute top-[11px] left-[11px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
            <div class="absolute top-[14px] left-[14px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
            <div class="absolute top-[18px] left-[18px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
            
            <div class="absolute top-[4px] left-[18px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
            <div class="absolute top-[8px] left-[14px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
            <div class="absolute top-[11px] left-[11px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
            <div class="absolute top-[14px] left-[8px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
            <div class="absolute top-[18px] left-[4px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          </div>
        </button>
        <div class="flex flex-col items-center gap-[1.5rem]">
          <div class="text-[var(--color-choco)] text-[2rem] font-['Xcu'] font-medium leading-none">
            튜토리얼을 끝내시겠습니까?
          </div>
          <div class="flex gap-4 justify-center mt-2">
            <button @click="finishTutorial" class="px-6 py-2 border-2 border-[var(--color-choco)] bg-[var(--color-butter)] text-[var(--color-choco)] font-['PfStardust30S'] text-xl rounded shadow-[2px_2px_0px_0px_var(--color-choco)] active:translate-y-1">
              네
            </button>
            <button @click="isSkipModalOpen = false" class="px-6 py-2 border-2 border-[var(--color-choco)] bg-white text-[var(--color-choco)] font-['PfStardust30S'] text-xl rounded shadow-[2px_2px_0px_0px_var(--color-choco)] active:translate-y-1">
              아니오
            </button>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
.tutorial-root { position: relative; width: 100vw; height: 100vh; overflow: hidden; cursor: pointer; }
.background-layer { position: absolute; inset: 0; z-index: 0; pointer-events: none; }

/* 오버레이 */
.full-overlay { position: absolute; inset: 0; background: rgba(0,0,0,0.6); z-index: 999; pointer-events: none; }
.spotlight-overlay { /* box-shadow set by JS */ }

/* 메시지 */
.message-box {
  position: absolute; transform: translate(-50%, -50%); z-index: 1000;
  max-width: 80%; width: max-content; pointer-events: none;
  color: var(--color-butter); font-family: 'PfStardust30S'; font-size: 1.8rem;
  text-shadow: -1px -1px 0 var(--color-syrup), 1px -1px 0 var(--color-syrup), -1px 1px 0 var(--color-syrup), 1px 1px 0 var(--color-syrup), 2px 2px 0 rgba(0,0,0,0.5);
  white-space: pre-wrap; text-align: center; line-height: 1.5;
  animation: float 2s infinite ease-in-out;
}
@keyframes float { 0%,100%{transform:translate(-50%,-50%);} 50%{transform:translate(-50%,-60%);} }

/* 아바타 Mock 스타일 */
.polaroid-frame { box-shadow: 0 4px 8px rgba(0,0,0,0.2); transition: transform 0.3s; }
.capture-btn { padding: 10px 20px; font-size: 1.5rem; }
.butter-btn { background: var(--color-butter); border: 2px solid var(--color-choco); color: var(--color-choco); border-radius: 4px; box-shadow: 2px 2px 0 var(--color-choco); }

/* 버튼 */
.btn-skip { position: fixed; top: 30px; right: 30px; z-index: 2000; background: var(--color-butter); border: 2px solid var(--color-syrup); padding: 8px 20px; font-family: 'PfStardust30S'; font-size: 1.2rem; border-radius: 8px; box-shadow: 2px 2px 0 var(--color-choco); cursor: pointer; color: var(--color-choco); }
.finish-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.8); z-index: 3000; display: flex; align-items: center; justify-content: center; }
.btn-finish { background: var(--color-butter); border: 4px solid var(--color-syrup); padding: 15px 40px; font-family: 'Xcu'; font-size: 2rem; border-radius: 20px; color: var(--color-choco); box-shadow: 4px 4px 0 var(--color-choco); cursor: pointer; }
.btn-finish:hover { transform: scale(1.1); }
</style>