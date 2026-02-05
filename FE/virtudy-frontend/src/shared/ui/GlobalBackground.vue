<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';

// 이미지 import
import bgPancake from '@/assets/bg_pancake.png';
import bgVillage from '@/assets/bg_village.png';
import bgCloud from '@/assets/bg_cloud.png';
import bgSky1 from '@/assets/bg_sky_1.png';
import bgSky2 from '@/assets/bg_sky_2.png';
import bgSky3 from '@/assets/bg_sky_3.png';

// Props 정의 및 기본값 설정
// type: 'pancake' | 'village' (기본값은 pancake)
const props = withDefaults(defineProps<{
  skyType?: 1 | 2 | 3; 
  type?: 'pancake' | 'village'; 
}>(), {
  skyType: 1,
  type: 'pancake'
});

// 하늘 이미지 선택 로직
const currentSky = computed(() => {
  switch (props.skyType) {
    case 1: return bgSky1;
    case 2: return bgSky2;
    case 3: return bgSky3;
    default: return bgSky1; 
  }
});

// 앞배경(팬케이크/마을) 이미지 선택 로직
const currentForeground = computed(() => {
  return props.type === 'village' ? bgVillage : bgPancake;
});

// 마우스 움직임 상태
const mouseX = ref(0);
const mouseY = ref(0);

// 움직임 강도 설정
const FACTOR_FOREGROUND = 0.03; // 기존 FACTOR_PANCAKE (가장 가까운 레이어)
const FACTOR_CLOUD = 0.01;      // 구름

// 마우스 핸들러
const handleMouseMove = (e: MouseEvent) => {
  const centerX = window.innerWidth / 2;
  const centerY = window.innerHeight / 2;
  
  mouseX.value = e.clientX - centerX;
  mouseY.value = e.clientY - centerY;
};

// 생명주기 훅
onMounted(() => {
  window.addEventListener('mousemove', handleMouseMove);
});

onUnmounted(() => {
  window.removeEventListener('mousemove', handleMouseMove);
});

// 스타일 바인딩 (transform)
const foregroundStyle = computed(() => ({
  transform: `translate(${mouseX.value * FACTOR_FOREGROUND}px, ${mouseY.value * FACTOR_FOREGROUND}px)`
}));

const cloudStyle = computed(() => ({
  transform: `translate(${mouseX.value * FACTOR_CLOUD}px, ${mouseY.value * FACTOR_CLOUD}px)`
}));
</script>

<template>
  <div class="global-background">
    <div class="layer sky" :style="{ backgroundImage: `url(${currentSky})` }"></div>

    <div class="layer cloud" :style="[{ backgroundImage: `url(${bgCloud})` }, cloudStyle]"></div>

    <div 
      class="layer pancake" 
      :style="[{ backgroundImage: `url(${currentForeground})` }, foregroundStyle]"
    ></div>
    
    <div class="content-layer">
      <slot></slot>
    </div>
  </div>
</template>

<style scoped>
.global-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100vw;
  min-height: 100vh;
  height: 100%;
  z-index: -1; 
  overflow: hidden;
  background-color: var(--color-cream2);
}

.layer {
  position: absolute;
  top: -5%; 
  left: -5%;
  width: 110%;
  height: 150%;
  background-position: center;
  background-size: cover;
  background-repeat: no-repeat;
  transition: transform 0.1s linear;
  object-fit: cover;
}

.sky {
  z-index: 1;
}

.cloud {
  z-index: 2;
}

/* 팬케이크 스타일을 마을에도 똑같이 적용 */
.layer.pancake {
  z-index: 3;
  height: 130%;
}

.content-layer {
  position: relative;
  z-index: 10; 
  width: 100%;
  height: 100%;
  overflow-y: auto; 
  overflow-x: hidden;
}
</style>