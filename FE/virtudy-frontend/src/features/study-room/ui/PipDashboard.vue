<script setup lang="ts">
import { computed } from 'vue';
import CharacterAvatar from '@/shared/ui/avatar/CharacterAvatar.vue';
import type { AvatarConfig } from '@/shared/types/common.types';
import { getScoreColor } from '../logic/scoreUtils';

const props = defineProps<{
  focusSeconds: number;
  myAvatarConfig: AvatarConfig;
  aiScore: number;
  aiStatus: string;
  // 팀원들 정보 (ID, Score)
  teammates: Array<{ id: string; score: number }>;
}>();

// 시간 포맷팅 (HH:MM:SS)
const formattedTime = computed(() => {
  const h = Math.floor(props.focusSeconds / 3600);
  const m = Math.floor((props.focusSeconds % 3600) / 60);
  const s = props.focusSeconds % 60;
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
});

// AI 상태 매핑
const getAiDrowsy = (status: string) => (status === 'SLEEP' ? 1 : 0);
const getAiPhone = (status: string) => (status === 'PHONE' ? 1 : 0);
const getAiAbsent = (status: string) => (status === 'AWAY' ? 1 : 0);

// 팀원 하트 5개 슬롯 생성
const teamHeartSlots = computed(() => {
  const maxSlots = 5;
  const slots = [];
  
  for (let i = 0; i < maxSlots; i++) {
    if (i < props.teammates.length) {
      // 팀원이 있는 슬롯: 해당 팀원의 점수 색상
      slots.push({
        filled: true,
        color: getScoreColor(props.teammates[i].score)
      });
    } else {
      // 팀원이 없는 슬롯: 회색 (비활성)
      slots.push({
        filled: false,
        color: '#b2bec3' // 회색
      });
    }
  }
  return slots;
});
</script>

<template>
  <div class="pip-container">
    
    <div class="top-section-wrapper">
      <div class="top-section">
        <div class="avatar-layer">
          <CharacterAvatar 
            :config="myAvatarConfig"
            :aiDrowsy="getAiDrowsy(aiStatus)"
            :aiPhone="getAiPhone(aiStatus)"
            :aiAbsent="getAiAbsent(aiStatus)"
            :isBlinking="false"
            mouthState="closed"
          />
        </div>

        <div class="overlay-layer">
          <div class="status-row">
            <span class="pixel-text">현재상태</span>
          </div>
          <div class="my-heart-wrapper">
            <svg viewBox="0 0 24 24" class="pixel-heart big" :style="{ fill: getScoreColor(aiScore) }">
                <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
            </svg>
          </div>
        </div>
      </div>
    </div>

    <div class="bottom-section">
      <div class="timer-text">{{ formattedTime }}</div>
      <div class="team-hearts-row">
        <div v-for="(slot, idx) in teamHeartSlots" :key="idx" class="heart-slot">
          <svg viewBox="0 0 24 24" class="pixel-heart small" :style="{ fill: slot.color }">
            <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
          </svg>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>

.pip-dashboard {
  width: 100vw;
  height: 100vh;
  /* 베이지색 배경 */
  background-color: #FFF4D9; 
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px;
  box-sizing: border-box;
  font-family: 'PfStardust30S', monospace, sans-serif;
  color: #5d4037; /* 진한 갈색 텍스트 */
  overflow: hidden;
}

/* 상단 아바타 영역 래퍼: 배경색이 늘어날 때 아바타만 둥둥 뜨지 않도록 처리 */
.top-section-wrapper {
  flex: 1; /* 남는 공간 차지 */
  width: 100%;
  display: flex;
  justify-content: center; /* 아바타 중앙 정렬 */
  background-color: #ffeaa7; /* 아바타 배경색이 좌우로 꽉 차게 */
}

/* === 상단 200x200 영역 === */
.top-section {
  position: relative; /* 겹치기 위한 기준점 */
  width: 200px;
  height: 200px;
  flex-shrink: 0;
}

/* Layer 1: 아바타 */
.avatar-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
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
}

.pixel-text {
  font-size: 1.5rem;
  font-weight: bold;
  color: #5d4037;
  text-shadow: 1px 1px 0px rgba(255,255,255,0.8); /* 가독성을 위한 흰 테두리 */
}

.my-heart-wrapper .pixel-heart.big {
  width: 40px;
  height: 40px;
  filter: drop-shadow(2px 2px 0px rgba(0,0,0,0.2));
  animation: beat 1.5s infinite;
}

/* === 하단 200x80 영역 === */
.bottom-section {
  width: 100%;
  min-height: 80px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #FFF4D9;
  border-top: 2px dashed #dcdcdc; /* 구분선 */
  padding-bottom: 10px;
}

.timer-text {
  font-size: 2.2rem;
  color: #FBC02D;
  text-shadow: 2px 2px 0px #5d4037;
  margin-bottom: 5px;
  line-height: 1;
}

.team-hearts-row {
  display: flex;
  gap: 5px; /* 하트 사이 간격 */
}

.pixel-heart.small {
  width: 20px;
  height: 20px;
  transition: fill 0.3s ease;
  filter: drop-shadow(1px 1px 0px rgba(0,0,0,0.1));
}

@keyframes beat {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}
</style>