// 아바타 전체 렌더링 컴포넌트입니다

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue';
import type { AvatarConfig } from '@/shared/types/common.types';
import AvatarPart from './AvatarPart.vue';

// ----------------------------------------------------------------------
// Props 정의
// ----------------------------------------------------------------------
const props = defineProps<{
  config: AvatarConfig; // 백엔드 외형 정보
  
  // AI 실시간 정보 (0 또는 1)
  aiDrowsy?: number;  // 1: 졸음/잠, 0: 깸
  aiAbsent?: number;  // 1: 자리 비움, 0: 있음
  aiPhone?: number;   // 1: 핸드폰 사용, 0: 안 함

  // 페이지별 위치 보정 (기본값은 현재 사용 중인 보정값)
  offsetX?: string;   // 예: '-16%'
  offsetY?: string;   // 예: '-40%'
}>();

// ----------------------------------------------------------------------
// 내부 애니메이션 상태 (Auto Animation State)
// ----------------------------------------------------------------------
const autoBlink = ref(false); // 내부적으로 계산된 눈 감음 여부
const autoMouth = ref<'closed' | 'slightly_open' | 'wide_open'>('closed'); // 내부 입 모양

// 타이머 ID 저장소 (컴포넌트 해제 시 정리용)
let blinkTimeoutId: number | undefined;
let mouthIntervalId: number | undefined;

// 눈 깜빡임 루프 함수 (재귀 호출)
const startBlinkLoop = () => {
  // 다음 깜빡임까지 대기 시간: 2초 ~ 7초 사이 랜덤
  const nextBlinkTime = Math.random() * 5000 + 2000;

  blinkTimeoutId = window.setTimeout(() => {
    autoBlink.value = true; // 눈 감기

    // 150ms 후 눈 뜨기
    setTimeout(() => {
      autoBlink.value = false;
      // ----------------------------------------------------
      // [Double Blink Logic] 
      // 30% 확률로 눈을 뜨자마자 다시 한 번 깜빡임
      // ----------------------------------------------------
      const isDoubleBlink = Math.random() < 0.3; 

      if (isDoubleBlink) {
        // 아주 짧은 찰나(50ms) 대기 후 다시 감기
        setTimeout(() => {
          autoBlink.value = true; // 두 번째 감기
          
          // 다시 150ms 후 완전히 뜨기
          setTimeout(() => {
            autoBlink.value = false;
            startBlinkLoop(); // 모든 동작 끝, 다음 루프 예약
          }, 150);
        }, 50);
      } else {
        // 더블 블링크 당첨 안 됨 -> 바로 다음 루프 예약
        startBlinkLoop();
      }
    }, 150);
  }, nextBlinkTime);
};

// 입 움직임 루프 함수 (setInterval 사용)
const startMouthLoop = () => {
  // 10초마다 실행
  mouthIntervalId = window.setInterval(() => {
    // 30% 확률로 입을 벌림 (공부 중 혼잣말 등)
    const shouldOpen = Math.random() > 0.7; 

    if (shouldOpen) {
      // 입 벌리기 (slightly_open)
      autoMouth.value = 'slightly_open';
      
      // 유지 시간: 3~6초 랜덤 (3000ms + 0~3000ms)
      const duration = 3000 + Math.random() * 3000;
      
      setTimeout(() => {
        autoMouth.value = 'closed';
      }, duration);
    }
  }, 10000); // 10초 주기
};

// 생명주기 훅: 마운트 시 시작, 언마운트 시 종료
onMounted(() => {
  startBlinkLoop();
  startMouthLoop();
});

onUnmounted(() => {
  clearTimeout(blinkTimeoutId);
  clearInterval(mouthIntervalId);
});

// ----------------------------------------------------------------------
// 데이터 정제 (외형 설정 매핑)
// ----------------------------------------------------------------------
const removePrefix = (value: string, prefix: string) => {
  if (!value || value === 'none') return 'none';
  return value.startsWith(prefix) ? value.replace(prefix, '') : value;
};

// 백엔드 값 매핑
const safeHairFront = computed(() => {
  const val = props.config.hairFront;
  if (val === 'hair_front_none') return 'none';
  if (val === 'bang') return 'bangs';
  return removePrefix(val, 'hair_front_');
});
const safeHairBack = computed(() => removePrefix(props.config.hairBack, 'hair_back_'));
const safeOutfit = computed(() => removePrefix(props.config.outfit, 'outfit_'));
const safeGlasses = computed(() => {
  return props.config.glasses === 'accessory_glasses' ? 'default' : 'none';
});

// ----------------------------------------------------------------------
// 상태 로직 구현 (AI 신호 0/1 처리)
// ----------------------------------------------------------------------

// [포즈 Pose] 우선순위: 부재 > 졸음 > 핸드폰 > 기본(공부)
const currentPose = computed(() => {
  // 1. 자리를 비움 (1) -> 의자는 그대로(studying 자세)지만 투명해짐
  if (props.aiAbsent === 1) return 'studying';

  // 2. 졸음 (1) -> 눈 감음
  if (props.aiDrowsy === 1) return 'sleeping';

  // 3. 핸드폰 (1) -> 폰 들기
  if (props.aiPhone === 1) return 'using_phone';

  // 4. 모두 0이면 -> 공부 중
  return 'studying';
});

// [눈 Eyes] 우선순위: 졸음 > 깜빡임 > 기본 설정
const currentEye = computed(() => {
  // 1. 졸음 상태(1)면 무조건 감은 눈
  if (props.aiDrowsy === 1) return 'closed';

  if (autoBlink.value) return 'closed'; // 2. 자동 깜빡임 중이면 감은 눈

  // 2. 평소엔 설정된 눈 ("eyes_cat" -> "cat")
  return removePrefix(props.config.eyes, 'eyes_');
});

// [입 Mouth] 내부 타이머 값 사용
const currentMouth = computed(() => {
  // 내부 타이머가 만든 상태 반환 ('closed', 'slightly_open' 등)
  return autoMouth.value;
});

// [투명도] 자리 비움(aiAbsent === 1)일 때만 반투명
const containerStyle = computed(() => ({
  opacity: props.aiAbsent === 1 ? 0.5 : 1,
  '--avatar-offset-x': props.offsetX ?? '-16%',
  '--avatar-offset-y': props.offsetY ?? '-40%',
  transform: 'translate(var(--avatar-offset-x, 0), var(--avatar-offset-y, 0))',
  transformOrigin: 'center bottom'
}));

// 고정값
const FIXED_FACE = 'default';
const SKIN_COLOR = '#ffe0bd';

</script>

<template>
  <div class="avatar-stage" :style="containerStyle">
    
    <AvatarPart 
      class="z-1" category="hair_back" 
      :option="safeHairBack" :color="config.hairColor" 
    />
    <AvatarPart 
      class="z-2" category="outfit" 
      :option="safeOutfit" :color="config.clothesColor" 
    />
    <AvatarPart 
      class="z-3" category="face_shape" 
      :option="FIXED_FACE" :color="SKIN_COLOR" 
    />
    <AvatarPart 
      class="z-4" category="mouth" 
      :option="currentMouth" 
    />
    <AvatarPart 
      class="z-5" category="eye" 
      :option="currentEye" 
    />
    <AvatarPart 
      class="z-6" category="accessory_glasses" 
      :option="safeGlasses" 
    />
    <AvatarPart 
      class="z-7" category="hair_front" 
      :option="safeHairFront" :color="config.hairColor" 
    />

    <AvatarPart 
      class="z-8"
      category="highlight" 
      option="default" 
    />
    
    <AvatarPart 
      class="z-9" category="pose" 
      :option="currentPose" 
    />

  </div>
</template>

<style scoped>
.avatar-stage { position: relative; width: 117%; height: 100%; transition: opacity 0.3s; }
.full-img { position: absolute; top: 0; left: 0; width: 100%; height: 100%; }
.z-1 { z-index: 1; }
.z-2 { z-index: 2; }
.z-3 { z-index: 3; }
.z-4 { z-index: 4; }
.z-5 { z-index: 5; }
.z-6 { z-index: 6; }
.z-7 { z-index: 7; }
.z-8 { z-index: 8; }
.z-9 { z-index: 9; }
</style>