<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue';
import type { AvatarConfig } from '@/shared/types/common.types';
import AvatarPart from './AvatarPart.vue';

// ----------------------------------------------------------------------
// Props 정의
// ----------------------------------------------------------------------
const props = defineProps<{
  config: AvatarConfig; 
  aiDrowsy?: number;  
  aiAbsent?: number;  
  aiPhone?: number;   
  offsetX?: string;   
  offsetY?: string;   
}>();

// ----------------------------------------------------------------------
// 유효한 파일 목록 (화이트리스트)
// ----------------------------------------------------------------------
const VALID_ASSETS: Record<string, string[]> = {
  hair_front: ['bangs', 'basic', 'center_part', 'short', 'side_part'],
  hair_back: ['bob', 'long_curly', 'long_straight', 'lowtail', 'short'],
  eye: ['cat', 'closed', 'droopy', 'round', 'small'],
  outfit: ['knit', 'round_neck', 'shirt'],
  accessory_glasses: ['default'],
};

// ----------------------------------------------------------------------
// 디폴트 값 정의
// ----------------------------------------------------------------------
const DEFAULTS = {
  hairFront: 'short',
  hairBack: 'short',
  hairColor: '#7a7069',
  eye: 'round',
  glasses: 'none', // 기본은 안경 없음
  outfit: 'round_neck',
  clothesColor: '#FFFFFF'
};

// ----------------------------------------------------------------------
// 핵심 로직: 유효성 검사 및 폴백 함수
// ----------------------------------------------------------------------
const getSafeOption = (category: string, rawValue: string, prefix: string, fallback: string) => {
  if (rawValue === 'none') return 'none';
  if (!rawValue) return fallback;

  // 접두사 제거
  let option = rawValue.startsWith(prefix) ? rawValue.replace(prefix, '') : rawValue;

  // 화이트리스트 검사
  const validList = VALID_ASSETS[category];
  
  if (validList && validList.includes(option)) {
    return option;
  }

  // 파일이 없으면 폴백(기본값) 반환
  return fallback;
};

// ----------------------------------------------------------------------
// 색상 처리 로직
// ----------------------------------------------------------------------
const displayHairColor = computed(() => {
  const origin = props.config.hairColor;
  if (!origin) return DEFAULTS.hairColor;
  if (origin.trim().toUpperCase() === '#2B2B2B') {
    return DEFAULTS.hairColor;
  }
  return origin;
});

const displayClothesColor = computed(() => {
  return props.config.clothesColor || DEFAULTS.clothesColor;
});

// ----------------------------------------------------------------------
// 내부 애니메이션 상태 (Blink / Mouth)
// ----------------------------------------------------------------------
const autoBlink = ref(false); 
const autoMouth = ref<'closed' | 'slightly_open' | 'wide_open'>('closed'); 
let blinkTimeoutId: number | undefined;
let mouthIntervalId: number | undefined;

const startBlinkLoop = () => {
  const nextBlinkTime = Math.random() * 5000 + 2000;
  blinkTimeoutId = window.setTimeout(() => {
    autoBlink.value = true; 
    setTimeout(() => {
      autoBlink.value = false;
      const isDoubleBlink = Math.random() < 0.3; 
      if (isDoubleBlink) {
        setTimeout(() => {
          autoBlink.value = true; 
          setTimeout(() => {
            autoBlink.value = false;
            startBlinkLoop(); 
          }, 150);
        }, 50);
      } else {
        startBlinkLoop();
      }
    }, 150);
  }, nextBlinkTime);
};

const startMouthLoop = () => {
  mouthIntervalId = window.setInterval(() => {
    const shouldOpen = Math.random() > 0.7; 
    if (shouldOpen) {
      autoMouth.value = 'slightly_open';
      const duration = 3000 + Math.random() * 3000;
      setTimeout(() => {
        autoMouth.value = 'closed';
      }, duration);
    }
  }, 10000); 
};

onMounted(() => {
  startBlinkLoop();
  startMouthLoop();
});

onUnmounted(() => {
  clearTimeout(blinkTimeoutId);
  clearInterval(mouthIntervalId);
});

// ----------------------------------------------------------------------
// Computed Properties
// ----------------------------------------------------------------------

// 1. 앞머리
const safeHairFront = computed(() => {
  return getSafeOption('hair_front', props.config.hairFront, 'hair_front_', DEFAULTS.hairFront);
});

// 2. 뒷머리
const safeHairBack = computed(() => {
  return getSafeOption('hair_back', props.config.hairBack, 'hair_back_', DEFAULTS.hairBack);
});

// 3. 옷
const safeOutfit = computed(() => {
  return getSafeOption('outfit', props.config.outfit, 'outfit_', DEFAULTS.outfit);
});

// 4. 안경 (수정됨)
const safeGlasses = computed(() => {
  const val = props.config.glasses;

  // 값이 'accessory_glasses'라면 -> 'default' (accessory_glasses_default.svg 로드)
  if (val === 'accessory_glasses') {
    return 'default';
  }

  // 값이 'none'이거나, 비어있거나, 알 수 없는 값이면 -> 'none' (렌더링 안 함)
  return 'none';
});

// [포즈]
const currentPose = computed(() => {
  if (props.aiAbsent === 1) return 'studying';
  if (props.aiDrowsy === 1) return 'sleeping';
  if (props.aiPhone === 1) return 'using_phone';
  return 'studying';
});

// [눈]
const currentEye = computed(() => {
  if (props.aiDrowsy === 1) return 'closed';
  if (autoBlink.value) return 'closed'; 
  return getSafeOption('eye', props.config.eyes, 'eyes_', DEFAULTS.eye);
});

// [입]
const currentMouth = computed(() => autoMouth.value);

// [투명도]
const containerStyle = computed(() => ({
  opacity: props.aiAbsent === 1 ? 0.5 : 1,
  '--avatar-offset-x': props.offsetX ?? '-16%',
  '--avatar-offset-y': props.offsetY ?? '-40%',
  transform: 'translate(var(--avatar-offset-x, 0), var(--avatar-offset-y, 0))',
  transformOrigin: 'center bottom'
}));

const FIXED_FACE = 'default';
</script>

<template>
  <div class="avatar-stage" :style="containerStyle">
    
    <AvatarPart 
      class="z-1" category="hair_back" 
      :option="safeHairBack" :color="displayHairColor" 
    />
    <AvatarPart 
      class="z-2" category="outfit" 
      :option="safeOutfit" :color="displayClothesColor" 
    />
    <AvatarPart 
      class="z-3" category="face_shape" 
      :option="FIXED_FACE" 
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
      :option="safeHairFront" :color="displayHairColor" 
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
