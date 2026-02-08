<script setup lang="ts">
<<<<<<< HEAD
import { computed, ref } from 'vue';
=======
import { computed, ref, watch } from 'vue';
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
import CharacterAvatar from '@/shared/ui/avatar/CharacterAvatar.vue';
import type { AvatarConfig } from '@/shared/types/common.types';
import { getScoreColor } from '../logic/scoreUtils';
import FlashbangEffect from '../ui/FlashbangEffect.vue';

<<<<<<< HEAD
// SVG를 문자열(raw string)로 가져옴 
import heartPixelSvg from '@/assets/room/heart_pixel.svg?raw';

// 이미지 에셋 임포트
import roomWoodBg from '@/assets/room/room_wood_bg.png';
import bgPhotoGreen from '@/assets/room/bg_photo_1.png';
import bgPhotoYellow from '@/assets/room/bg_photo_2.png';
import bgPhotoRed from '@/assets/room/bg_photo_3.png';
import footerBarBg from '@/assets/room/footer_bar_bg.png';

// BgState 타입 정의
type BgState = 'GREEN' | 'YELLOW' | 'RED';

=======
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
const props = defineProps<{
  focusSeconds: number;
  myAvatarConfig: AvatarConfig;
  aiScore: number;
  aiStatus: string;
  // 팀원들 정보 (ID, Score)
  teammates: Array<{ id: string; score: number }>;
  isWakeUpAvailable: boolean;
  onOpenWakeUpModal: () => void;
  isStunned?: boolean;
<<<<<<< HEAD
  bgState: BgState; // 배경 상태
=======
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
}>();

// 안내 메시지 표시 상태
const showGuideMessage = ref(false);

// 깨우기 버튼 클릭 핸들러 (PIP 전용)
const handlePipWakeUpClick = () => {
<<<<<<< HEAD
  props.onOpenWakeUpModal();
  showGuideMessage.value = true;
=======
  // 1. 메인 윈도우의 모달 열기 함수 실행
  props.onOpenWakeUpModal();
  
  // 2. 안내 메시지 표시
  showGuideMessage.value = true;

  // 3. 10초 뒤에 다시 버튼/상태 표시로 복귀
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
  setTimeout(() => {
    showGuideMessage.value = false;
  }, 10000);
};

// 시간 포맷팅 (HH:MM:SS)
const formattedTime = computed(() => {
  const h = Math.floor(props.focusSeconds / 3600);
  const m = Math.floor((props.focusSeconds % 3600) / 60);
  const s = props.focusSeconds % 60;
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
});

<<<<<<< HEAD
// 배경 이미지 동적 선택 (채우기 옵션용)
const currentBgImage = computed(() => {
  switch (props.bgState) {
    case 'GREEN': return `url(${bgPhotoGreen})`;
    case 'YELLOW': return `url(${bgPhotoYellow})`;
    case 'RED': return `url(${bgPhotoRed})`;
    default: return `url(${bgPhotoGreen})`;
  }
});

// AI 상태 매핑 Helpers
=======
// AI 상태 매핑
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
const getAiDrowsy = (status: string) => (status === 'SLEEP' ? 1 : 0);
const getAiPhone = (status: string) => (status === 'PHONE' ? 1 : 0);
const getAiAbsent = (status: string) => (status === 'AWAY' ? 1 : 0);

<<<<<<< HEAD
// 현재 상태 텍스트 로직
const currentStatusText = computed(() => {
  switch (props.aiStatus) {
    case 'SLEEP': return '자는 중';
    case 'PHONE': return '핸드폰 하는 중';
    case 'AWAY': return '자리비움';
    case 'FOCUS': 
    default: return '집중 중!';
  }
});

// 깨우기 버튼 표시 여부 로직
// 1. 깨우기가 활성화 상태여야 함 (isWakeUpAvailable)
// 2. 동시에 내 상태가 'FOCUS'(집중 중)여야 함
const showWakeUpButton = computed(() => {
  const isFocusing = !props.aiStatus || props.aiStatus === 'FOCUS';
  return props.isWakeUpAvailable && isFocusing;
});

// 색상 유틸리티
=======
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
const shadeColor = (hex: string, percent: number) => {
  const normalized = hex.replace('#', '');
  if (normalized.length !== 6) return hex;
  const num = parseInt(normalized, 16);
  const r = (num >> 16) & 255;
  const g = (num >> 8) & 255;
  const b = num & 255;
  const t = percent < 0 ? 0 : 255;
  const p = Math.abs(percent);
  const rr = Math.round((t - r) * p) + r;
  const gg = Math.round((t - g) * p) + g;
  const bb = Math.round((t - b) * p) + b;
  const toHex = (c: number) => c.toString(16).padStart(2, '0');
  return `#${toHex(rr)}${toHex(gg)}${toHex(bb)}`;
};

<<<<<<< HEAD
// 하트 스타일 생성
=======
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
const getHeartStyle = (score: number) => {
  const base = getScoreColor(score);
  return {
    '--heart-base': base,
    '--heart-line': shadeColor(base, -0.35),
    '--heart-highlight': '#FFFFDB',
    '--heart-shadow': shadeColor(base, -0.2)
  };
};

const getEmptyHeartStyle = () => ({
  '--heart-base': '#D9D9D9',
  '--heart-line': '#B0B0B0',
  '--heart-highlight': '#F2F2F2',
  '--heart-shadow': '#C0C0C0'
});

<<<<<<< HEAD
// 팀원 하트 슬롯
=======
// 팀원 하트 5개 슬롯 생성
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
const teamHeartSlots = computed(() => {
  const maxSlots = 5;
  const slots: Array<{ filled: boolean; style: Record<string, string> }> = [];
  
  for (let i = 0; i < maxSlots; i++) {
    const teammate = props.teammates[i];
    if (teammate) {
<<<<<<< HEAD
=======
      // ????? ??? ???: ??? ????? ??? ???
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
      slots.push({
        filled: true,
        style: getHeartStyle(teammate.score)
      });
    } else {
<<<<<<< HEAD
=======
      // ????? ??? ???: ??? (?????
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
      slots.push({
        filled: false,
        style: getEmptyHeartStyle()
      });
    }
  }
  return slots;
});
</script>

<template>
<<<<<<< HEAD
  <div class="pip-container" :style="{ backgroundImage: `url(${roomWoodBg})` }">
    
    <svg class="heart-symbols" aria-hidden="true" style="position: absolute; width: 0; height: 0; overflow: hidden;">
=======
  <div class="pip-container">
    <svg class="heart-symbols" aria-hidden="true" style="position: absolute; width: 0; height: 0; overflow: hidden;">
      <symbol id="heart-pixel-symbol" viewBox="0 0 32 24">
            <g class="heart-line">
            <path d="M13.3334 2.84446H16V5.51113H13.3334V2.84446Z"/>
            <path d="M10.6667 0.177794H13.3334L13.3334 2.84446H10.6667V0.177794Z"/>
            <path d="M8.00002 0.177794H10.6667V2.84446H8.00002V0.177794Z"/>
            <path d="M5.33335 2.66667H8.00002V5.33333H5.33335V2.66667Z"/>
            <path d="M2.66669 5.51113H5.33335V8.17779H2.66669V5.51113Z"/>
            <path d="M2.66669 8H5.33335V10.6667H2.66669V8Z"/>
            <path d="M5.33335 10.6667H8.00002V13.3333H5.33335V10.6667Z"/>
            <path d="M8.00002 13.3333L10.6667 13.3333V16H8.00002L8.00002 13.3333Z"/>
            <path d="M10.6667 16H13.3334V18.6667H10.6667V16Z"/>
            <path d="M13.3334 18.6667H16V21.3333H13.3334L13.3334 18.6667Z"/>
            <path d="M16 21.3333L18.6667 21.3333V24H16V21.3333Z"/>
            <path d="M18.6667 18.6667H21.3334V21.3333L18.6667 21.3333L18.6667 18.6667Z"/>
            <path d="M21.3334 16H24V18.6667H21.3334L21.3334 16Z"/>
            <path d="M24 13.3333H26.6667V16H24V13.3333Z"/>
            <path d="M26.6667 10.6667H29.3334V13.3333L26.6667 13.3333V10.6667Z"/>
            <path d="M29.3334 8H32V10.6667H29.3334L29.3334 8Z"/>
            <path d="M29.3334 5.51113H32V8.17779H29.3334V5.51113Z"/>
            <path d="M26.6667 2.66667H29.3334V5.33333H26.6667V2.66667Z"/>
            <path d="M24 0H26.6667V2.66667H24V0Z"/>
            <path d="M21.3334 0H24V2.66667H21.3334V0Z"/>
            <path d="M18.6667 2.66667H21.3334L21.3334 5.33333H18.6667V2.66667Z"/>
            <path d="M16 5.51113H18.6667V8.17779H16V5.51113Z"/>
            </g>
            <g class="heart-highlight">
            <path d="M10.6667 5.33337H13.3333V8.00004H10.6667V5.33337Z"/>
            <path d="M8 8.00004L10.6667 8.00004L10.6667 10.6667H8V8.00004Z"/>
            </g>
            <g class="heart-base">
            <path d="M26.6666 2.66663H24V5.33329H26.6666V2.66663Z"/>
            <path d="M10.6666 13.3333H13.3333V16H10.6666V13.3333Z"/>
            <path d="M13.3333 13.3333H16V16H13.3333V13.3333Z"/>
            <path d="M16 13.3333H18.6666V16H16V13.3333Z"/>
            <path d="M16 10.6666H18.6666V13.3333H16V10.6666Z"/>
            <path d="M16 7.99996H18.6666V10.6666H16V7.99996Z"/>
            <path d="M10.6666 2.84442H13.3333V5.51109H10.6666V2.84442Z"/>
            <path d="M7.99998 2.84442H10.6666V5.51109H7.99998V2.84442Z"/>
            <path d="M5.33331 5.33329H7.99998V7.99996H5.33331V5.33329Z"/>
            <path d="M7.99998 5.33329H10.6666V7.99996H7.99998V5.33329Z"/>
            <path d="M13.3333 5.51109H16V8.17775H13.3333L13.3333 5.51109Z"/>
            <path d="M13.3333 7.99996H16V10.6666H13.3333V7.99996Z"/>
            <path d="M13.3333 10.6666H16V13.3333H13.3333L13.3333 10.6666Z"/>
            <path d="M10.6666 10.6666H13.3333L13.3333 13.3333H10.6666V10.6666Z"/>
            <path d="M10.6666 7.99996L13.3333 7.99996V10.6666H10.6666L10.6666 7.99996Z"/>
            <path d="M7.99998 10.6666H10.6666V13.3333L7.99998 13.3333V10.6666Z"/>
            <path d="M5.33331 7.99996H7.99998L7.99998 10.6666H5.33331V7.99996Z"/>
            <path d="M16 18.6666H18.6666V21.3333H16V18.6666Z"/>
            <path d="M18.6666 16H21.3333V18.6666L18.6666 18.6666V16Z"/>
            <path d="M16 16H18.6666V18.6666H16V16Z"/>
            <path d="M13.3333 16H16V18.6666L13.3333 18.6666L13.3333 16Z"/>
            <path d="M21.3333 13.3333H24V16H21.3333L21.3333 13.3333Z"/>
            <path d="M21.3333 10.6666H24V13.3333H21.3333V10.6666Z"/>
            <path d="M21.3333 7.99996H24V10.6666H21.3333V7.99996Z"/>
            <path d="M18.6666 5.33329H21.3333L21.3333 7.99996H18.6666L18.6666 5.33329Z"/>
            <path d="M18.6666 7.99996H21.3333V10.6666H18.6666V7.99996Z"/>
            <path d="M18.6666 10.6666H21.3333V13.3333H18.6666V10.6666Z"/>
            <path d="M18.6666 13.3333H21.3333L21.3333 16H18.6666V13.3333Z"/>
            <path d="M21.3333 5.33329H24L24 7.99996H21.3333L21.3333 5.33329Z"/>
            <path d="M21.3333 2.66663H24V5.33329H21.3333L21.3333 2.66663Z"/>
            <path d="M24 2.66663H26.6666V5.33329H24V2.66663Z"/>
            <path d="M24 5.33329H26.6666V7.99996L24 7.99996L24 5.33329Z"/>
            <path d="M24 7.99996L26.6666 7.99996V10.6666H24V7.99996Z"/>
            <path d="M24 10.6666H26.6666V13.3333L24 13.3333V10.6666Z"/>
            <path d="M26.6666 5.33329H29.3333V7.99996H26.6666V5.33329Z"/>
            <path d="M26.6666 7.99996H29.3333V10.6666H26.6666V7.99996Z"/>
            </g>
            <g id="heart_shadow">
            <path d="M13.3333 21.3333H16V24H13.3333V21.3333Z"/>
            <path d="M10.6667 18.6667H13.3333L13.3333 21.3333L10.6667 21.3333V18.6667Z"/>
            <path d="M16 2.84446H18.6667V5.51113H16V2.84446Z"/>
            <path d="M8 16H10.6667V18.6667H8V16Z"/>
            <path d="M5.33333 13.3333H8V16H5.33333V13.3333Z"/>
            <path d="M2.66667 10.6667H5.33333V13.3333L2.66667 13.3333V10.6667Z"/>
            <path d="M0 8H2.66667V10.6667H0V8Z"/>
            <path d="M0 5.51113H2.66667V8.17779H0V5.51113Z"/>
            <path d="M2.66667 2.84446H5.33333V5.51113H2.66667V2.84446Z"/>
            <path d="M5.33333 0.177794H8V2.84446H5.33333V0.177794Z"/>
            <path d="M18.6667 0H21.3333V2.66667H18.6667V0Z"/>
            </g>
            </symbol>
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
    </svg>
    
    <FlashbangEffect :visible="!!isStunned" />

    <div v-if="showGuideMessage" class="guide-overlay">
      <div class="guide-text">
        ⚡<br>
        깨우기가 활성화됐어요!<br>
        스터디룸으로 돌아가<br>
        팀원을 깨워보세요!
      </div>
    </div>

    <template v-else>
<<<<<<< HEAD
      <div class="frame-border">
        
        <div class="top-section-wrapper" :style="{ backgroundImage: currentBgImage }">
          <div class="top-section">
            <div class="avatar-layer">
              <CharacterAvatar 
                :config="myAvatarConfig"
                :aiDrowsy="getAiDrowsy(aiStatus)"
                :aiPhone="getAiPhone(aiStatus)"
                :aiAbsent="getAiAbsent(aiStatus)"
                :isBlinking="false"
                offsetX="0%"
                offsetY="7%"
                mouthState="closed"
              />
            </div>

            <div class="overlay-layer" :class="{ 'wakeup-mode': showWakeUpButton }">
              <div class="status-row">
                <template v-if="showWakeUpButton">
                  <button 
                    class="btn-pixel-action btn-wakeup active"
                    @click="handlePipWakeUpClick"
                  >
                    깨우기!
                  </button>
                </template>
                <template v-else>
                  <span class="pixel-text">{{ currentStatusText }}</span>
                </template>
              </div>
              
              <div v-if="!showWakeUpButton" class="my-heart-wrapper">
                <div 
                  class="pixel-heart-icon big" 
                  :style="getHeartStyle(aiScore)"
                  v-html="heartPixelSvg"
                ></div>
              </div>
            </div>
          </div>
        </div>

        <div class="bottom-section" :style="{ backgroundImage: `url(${footerBarBg})` }">
          <div class="timer-text">{{ formattedTime }}</div>
          <div class="team-hearts-row">
            <div v-for="(slot, idx) in teamHeartSlots" :key="idx" class="heart-slot">
                <div 
                  class="pixel-heart-icon small" 
                  :style="slot.style"
                  v-html="heartPixelSvg"
                ></div>
            </div>
          </div>
        </div>

      </div> 
=======
      <div class="top-section-wrapper">
        <div class="top-section">
          <div class="avatar-layer">
            <CharacterAvatar 
              :config="myAvatarConfig"
              :aiDrowsy="getAiDrowsy(aiStatus)"
              :aiPhone="getAiPhone(aiStatus)"
              :aiAbsent="getAiAbsent(aiStatus)"
              :isBlinking="false"
              offsetX="0%"
              offsetY="9%"
              mouthState="closed"
            />
          </div>

          <div class="overlay-layer">
            <div class="status-row">
              <template v-if="isWakeUpAvailable">
                <button 
                  class="btn-pixel-action btn-wakeup active"
                  @click="handlePipWakeUpClick"
                >
                  깨우기!
                </button>
              </template>
              <template v-else>
                <span class="pixel-text">현재 상태</span>
              </template>
            </div>
            
        <div v-if="!isWakeUpAvailable" class="my-heart-wrapper">
              <svg viewBox="0 0 32 24" class="pixel-heart big" :style="getHeartStyle(aiScore)">
                <use href="#heart-pixel-symbol" />
              </svg>
            </div>
          </div>
        </div>
      </div>

      <div class="bottom-section">
        <div class="timer-text">{{ formattedTime }}</div>
        <div class="team-hearts-row">
          <div v-for="(slot, idx) in teamHeartSlots" :key="idx" class="heart-slot">
            <svg viewBox="0 0 32 24" class="pixel-heart small" :style="slot.style">
              <use href="#heart-pixel-symbol" />
            </svg>
          </div>
        </div>
      </div>
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
    </template>

  </div>
</template>

<style scoped>
<<<<<<< HEAD
/* 폰트 및 기본 설정 */
.pip-container {
  width: 100vw;
  height: 100vh;
=======

.pip-container {
  width: 100vw;
  height: 100vh;
  /* 베이지색 배경 */
  background-color: #FFF4D9; 
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px;
  box-sizing: border-box;
  font-family: 'PfStardust30S', monospace, sans-serif;
<<<<<<< HEAD
  color: #5d4037;
  overflow: hidden;
  position: relative;
  background-color: #FFF4D9;
  background-repeat: no-repeat;
  background-position: center;
  background-size: cover;
=======
  color: #5d4037; /* 진한 갈색 텍스트 */
  overflow: hidden;
  position: relative;
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
}

/* 안내 메시지 오버레이 */
.guide-overlay {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
<<<<<<< HEAD
  background-color: rgba(255, 244, 217, 0.95);
=======
  background-color: rgba(255, 244, 217, 0.95); /* 배경색과 동일하되 약간 투명 */
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
  display: flex; align-items: center; justify-content: center;
  z-index: 50; text-align: center;
}
.guide-text {
  font-size: 1.2rem;
  line-height: 1.5;
  color: #805143;
  font-weight: bold;
  animation: fadeIn 0.5s ease-out;
}
@keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }

<<<<<<< HEAD
/* 액자 프레임 스타일 */
.frame-border {
  border: 5px solid #805143;
  box-shadow: 5px 5px 0px rgba(0,0,0,0.2);
  display: flex;
  flex-direction: column;
  background-color: transparent;
  width: 100%;
  max-width: 240px;
  overflow: hidden; 
  border-radius: 2px;
}

/* 상단 섹션 */
=======
/* 상단 아바타 영역 래퍼: 배경색이 늘어날 때 아바타만 둥둥 뜨지 않도록 처리 */
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
.top-section-wrapper {
  flex: 0 0 auto;
  width: 100%;
  display: flex;
<<<<<<< HEAD
  justify-content: center;
  background-repeat: no-repeat;
  background-size: cover; 
  background-position: center;
  background-color: transparent;
  padding-bottom: 6px;
}

.top-section {
  position: relative;
  width: 100%;
=======
  justify-content: center; /* 아바타 중앙 정렬 */
  background-color: #ffeaa7; /* 아바타 배경색이 좌우로 꽉 차게 */
  padding-bottom: 6px;
}

/* === 상단 200x200 영역 === */
.top-section {
  position: relative; /* 겹치기 위한 기준점 */
  width: 200px;
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
  height: 230px;
  flex-shrink: 0;
}

/* Layer 1: 아바타 */
.avatar-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
<<<<<<< HEAD
  z-index: 1;
  display: flex;
  justify-content: center;
  align-items: flex-end;
}

/* Layer 2: 오버레이 */
.overlay-layer {
  position: absolute;
  top: 10px; /* 기본 위치 */
  left: 0;
  width: 100%;
  z-index: 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  transition: top 0.3s ease; /* 위치 변경 시 부드럽게 */
}

/* 깨우기 모드일 때 위치 변경 */
.overlay-layer.wakeup-mode {
  top: 20px;
=======
  z-index: 1; /* 아래층 */
  display: flex;
  justify-content: center;
  align-items: flex-end; /* 아바타 바닥 정렬 */
}

/* Layer 2: 오버레이 (텍스트 + 내 하트) */
.overlay-layer {
  position: absolute;
  top: 10px;
  left: 0;
  width: 100%;
  z-index: 2; /* 위층 */
  display: flex;
  flex-direction: column;
  align-items: center; /* 가운데 정렬 */
  gap: 5px;
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
}

.pixel-text {
  font-size: 1.5rem;
  font-weight: bold;
  color: #5d4037;
<<<<<<< HEAD
  text-shadow: 1px 1px 0px rgba(255,255,255,0.8);
}

/* 하트 스타일링 */
.pixel-heart-icon.big {
  width: 40px;
  height: 30px;
  filter: drop-shadow(2px 2px 0px rgba(0,0,0,0.2));
  animation: beat 1.5s infinite;
}
.pixel-heart-icon.big :deep(svg) { width: 100%; height: 100%; }

.pixel-heart-icon.small {
  width: 32px;
  height: 24px;
  transition: all 0.3s ease;
  filter: drop-shadow(1px 1px 0px rgba(0,0,0,0.1));
}
.pixel-heart-icon.small :deep(svg) { width: 100%; height: 100%; }

/* SVG 내부 ID 타겟팅 */
.pixel-heart-icon :deep(#heart_line path) { 
  fill: var(--heart-line, #668128); 
  transition: fill 0.3s ease;
}
.pixel-heart-icon :deep(#heart_base path) { 
  fill: var(--heart-base, #B8D576); 
  transition: fill 0.3s ease;
}
.pixel-heart-icon :deep(#heart_shadow path) { 
  fill: var(--heart-shadow, #91B248); 
  transition: fill 0.3s ease;
}
.pixel-heart-icon :deep(#heart_highlight path) { 
  fill: var(--heart-highlight, #FFFFDB); 
}

/* 하단 섹션 */
=======
  text-shadow: 1px 1px 0px rgba(255,255,255,0.8); /* 가독성을 위한 흰 테두리 */
}

.my-heart-wrapper .pixel-heart.big {
  width: 40px;
  height: 30px;
  shape-rendering: crispEdges;
  image-rendering: pixelated;
  filter: drop-shadow(2px 2px 0px rgba(0,0,0,0.2));
  animation: beat 1.5s infinite;
}

/* === 하단 200x80 영역 === */
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
.bottom-section {
  width: 100%;
  min-height: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
<<<<<<< HEAD
  background-color: transparent;
  background-repeat: no-repeat;
  background-size: cover;
  background-position: center;
  
  border-top: 2px solid #805143;
=======
  background-color: #FFF4D9;
  border-top: 2px dashed #dcdcdc; /* 구분선 */
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
  padding-bottom: 10px;
  padding: 6px 0;
}

.timer-text {
  font-size: 2.2rem;
  color: #FBC02D;
  text-shadow: 2px 2px 0px #5d4037;
  margin-bottom: 4px;
  line-height: 1;
}

.team-hearts-row {
  display: flex;
<<<<<<< HEAD
  gap: 5px;
}

=======
  gap: 5px; /* 하트 사이 간격 */
}

.pixel-heart.small {
  width: 32px;
  height: 24px;
  shape-rendering: crispEdges;
  image-rendering: pixelated;
  transition: filter 0.3s ease;
  filter: drop-shadow(1px 1px 0px rgba(0,0,0,0.1));
}

.heart-line path { fill: var(--heart-line, #668128); }
.heart-base path { fill: var(--heart-base, #B8D576); }
.heart-highlight path { fill: var(--heart-highlight, #FFFFDB); }
.heart-shadow path,
#heart_shadow path { fill: var(--heart-shadow, var(--heart-line, #668128)); }

>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
@keyframes beat {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

<<<<<<< HEAD
.btn-wakeup {
  background: #FFD966;
=======
/* 깨우기 버튼 스타일 (PIP 전용) */
.btn-wakeup {
  background: #FFD966; /* 기본 노란색 */
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
  color: #805143;
  border: 2px solid #805143;
  padding: 5px 15px;
  font-family: 'PfStardust30S';
  font-size: 1.2rem;
  border-radius: 20px;
  cursor: pointer;
  box-shadow: 0px 4px 0px #805143;
<<<<<<< HEAD
=======
  /* 애니메이션: 활성화 시에만 작동 */
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
  animation: bounce 1s infinite;
}

@keyframes bounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-3px); } }
<<<<<<< HEAD
</style>
=======

.feedback-toast {
    position: fixed; top: 20%; left: 50%; transform: translateX(-50%);
    background: rgba(0,0,0,0.8); color: #FFF2CC;
    padding: 15px 30px; border-radius: 30px;
    font-family: 'PfStardust30S'; z-index: 3000;
    animation: fadeOut 2s forwards;
}

@keyframes fadeOut { 0% {opacity: 1;} 80% {opacity: 1;} 100% {opacity: 0;} }
</style>
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
