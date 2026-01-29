// 아바타 전체 렌더링 컴포넌트입니다

<script setup lang="ts">
import { computed } from 'vue';
import type { AvatarConfig } from '@/shared/types/common.types';
import AvatarPart from './AvatarPart.vue';

// ----------------------------------------------------------------------
// 1. Props 정의
// ----------------------------------------------------------------------
const props = defineProps<{
  config: AvatarConfig; // 백엔드 외형 정보
  
  // AI 실시간 정보 (0 또는 1)
  aiDrowsy?: number;  // 1: 졸음/잠, 0: 깸
  aiAbsent?: number;  // 1: 자리 비움, 0: 있음
  aiPhone?: number;   // 1: 핸드폰 사용, 0: 안 함
  
  // [Placeholder] 아직 데이터 형식이 미정인 부분
  isBlinking?: boolean; 
  mouthState?: 'closed' | 'slightly_open' | 'wide_open';
}>();

// ----------------------------------------------------------------------
// 2. 데이터 정제 (외형 설정 매핑)
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
// 3. 상태 로직 구현 (AI 신호 0/1 처리)
// ----------------------------------------------------------------------

// [포즈 Pose] 우선순위: 부재 > 졸음 > 핸드폰 > 기본(공부)
const currentPose = computed(() => {
  // 1. 자리를 비움 (1) -> 의자는 그대로(studying 자세)지만 투명해짐
  if (props.aiAbsent === 1) return 'studying';

  // 2. 졸음 (1) -> 엎드려 잠
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
  
  // 2. 깜빡임 신호가 오면 감은 눈
  if (props.isBlinking) return 'closed';

  // 3. 평소엔 설정된 눈 ("eyes_cat" -> "cat")
  return removePrefix(props.config.eyes, 'eyes_');
});

// [입 Mouth] 아직 데이터가 없으므로 Props 그대로 사용 (기본값 closed)
const currentMouth = computed(() => props.mouthState || 'closed');

// [투명도] 자리 비움(aiAbsent === 1)일 때만 반투명
const containerStyle = computed(() => ({
  opacity: props.aiAbsent === 1 ? 0.5 : 1
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
.avatar-stage { position: relative; width: 100%; height: 100%; transition: opacity 0.3s; }
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