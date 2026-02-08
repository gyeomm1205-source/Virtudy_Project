<script setup lang="ts">
import { computed, ref } from 'vue';
import CharacterAvatar from '@/shared/ui/avatar/CharacterAvatar.vue';
import type { AvatarConfig } from '@/shared/types/common.types';
import { getScoreColor } from '../logic/scoreUtils';
import FlashbangEffect from '../ui/FlashbangEffect.vue';

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
  bgState: BgState; // 배경 상태
}>();

// 안내 메시지 표시 상태
const showGuideMessage = ref(false);

// 깨우기 버튼 클릭 핸들러 (PIP 전용)
const handlePipWakeUpClick = () => {
  props.onOpenWakeUpModal();
  showGuideMessage.value = true;
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
const getAiDrowsy = (status: string) => (status === 'SLEEP' ? 1 : 0);
const getAiPhone = (status: string) => (status === 'PHONE' ? 1 : 0);
const getAiAbsent = (status: string) => (status === 'AWAY' ? 1 : 0);

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

// 하트 스타일 생성
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

// 팀원 하트 슬롯
const teamHeartSlots = computed(() => {
  const maxSlots = 5;
  const slots: Array<{ filled: boolean; style: Record<string, string> }> = [];
  
  for (let i = 0; i < maxSlots; i++) {
    const teammate = props.teammates[i];
    if (teammate) {
      slots.push({
        filled: true,
        style: getHeartStyle(teammate.score)
      });
    } else {
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
  <div class="pip-container" :style="{ backgroundImage: `url(${roomWoodBg})` }">
    
    <svg class="heart-symbols" aria-hidden="true" style="position: absolute; width: 0; height: 0; overflow: hidden;">
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
    </template>

  </div>
</template>

<style scoped>
/* 폰트 및 기본 설정 */
.pip-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px;
  box-sizing: border-box;
  font-family: 'PfStardust30S', monospace, sans-serif;
  color: #5d4037;
  overflow: hidden;
  position: relative;
  background-color: #FFF4D9;
  background-repeat: no-repeat;
  background-position: center;
  background-size: cover;
}

/* 안내 메시지 오버레이 */
.guide-overlay {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background-color: rgba(255, 244, 217, 0.95);
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
.top-section-wrapper {
  flex: 0 0 auto;
  width: 100%;
  display: flex;
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
}

.pixel-text {
  font-size: 1.5rem;
  font-weight: bold;
  color: #5d4037;
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
.bottom-section {
  width: 100%;
  min-height: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: transparent;
  background-repeat: no-repeat;
  background-size: cover;
  background-position: center;
  
  border-top: 2px solid #805143;
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
  gap: 5px;
}

@keyframes beat {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.btn-wakeup {
  background: #FFD966;
  color: #805143;
  border: 2px solid #805143;
  padding: 5px 15px;
  font-family: 'PfStardust30S';
  font-size: 1.2rem;
  border-radius: 20px;
  cursor: pointer;
  box-shadow: 0px 4px 0px #805143;
  animation: bounce 1s infinite;
}

@keyframes bounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-3px); } }
</style>