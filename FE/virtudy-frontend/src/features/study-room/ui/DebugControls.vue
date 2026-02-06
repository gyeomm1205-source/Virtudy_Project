<template>
  <div class="debug-panel">
    <h3>🛠️ 개발자 테스트 패널</h3>
    <div class="btn-group">
      <button @click="addDummyMember">1. 가짜 팀원 입장 (80점)</button>
      <button @click="makeDummyDrowsy">2. 팀원 점수 깎기 (30점)</button>
      <button @click="triggerStage3" class="btn-danger">3. 📉 점수 0점 (3단계 진입)</button>
      <button @click="triggerSelfStun">4. 😵 내가 섬광탄 맞기</button>
      <button @click="reset">🔄 초기화</button>
    </div>
    <div class="status-log">
      <p>팀원 수: {{ Object.keys(scores).length }}명</p>
      <p v-if="scores[DUMMY_ID] !== undefined">
        Dummy 점수: {{ scores[DUMMY_ID] }}점
      </p>
      <p>내 피격 상태: {{ isStunned ? 'ON' : 'OFF' }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { type Ref } from 'vue';

const props = defineProps<{
  scores: Record<string, number>;
  names: Record<string, string>;
  isStunned: boolean;
  onTriggerStun: () => void;
}>();

// 가짜 팀원 ID
const DUMMY_ID = 'test-dummy-01';

// 1. 가짜 팀원 추가 (80점)
const addDummyMember = () => {
  props.names[DUMMY_ID] = '졸린 개발자';
  props.scores[DUMMY_ID] = 80; 
  console.log('🧪 가짜 팀원 입장 완료 (80점)');
};

// 2. 점수 깎기 (30점 - 2단계 YELLOW 테스트용)
const makeDummyDrowsy = () => {
  if (props.scores[DUMMY_ID] === undefined) addDummyMember();
  
  props.scores[DUMMY_ID] = 30;
  console.log('🧪 팀원 점수 30점 -> 평균 하락 (YELLOW 예상)');
};

// [NEW] 3. 3단계 진입 (0점 - 3단계 RED 테스트용)
const triggerStage3 = () => {
  // 팀원이 없으면 일단 만듭니다
  if (props.scores[DUMMY_ID] === undefined) {
    props.names[DUMMY_ID] = '위험한 개발자';
  }
  
  // 점수를 0점으로 만들어 평균을 급격히 떨어뜨립니다.
  // (내 점수가 100점이라도, (100 + 0) / 2 = 50점이 되어 RED 구간 진입)
  props.scores[DUMMY_ID] = 0;
  console.log('🧪 팀원 점수 0점 -> 3단계(RED) 배경 진입');
};

// 4. 내가 맞기
const triggerSelfStun = () => {
  console.log('🧪 내 화면에 섬광탄 강제 발사');
  props.onTriggerStun();
};

const reset = () => {
  delete props.names[DUMMY_ID];
  delete props.scores[DUMMY_ID];
};
</script>

<style scoped>
.debug-panel {
  position: fixed;
  left: 20px;
  background: rgba(0, 0, 0, 0.85);
  padding: 15px;
  border-radius: 8px;
  border: 2px solid red;
  z-index: 10000;
  color: white;
  font-family: 'PfStardust30S';
  width: 250px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.5);
}
h3 { margin: 0 0 10px 0; font-size: 14px; color: #ff5555; }
.btn-group { display: flex; flex-direction: column; gap: 6px; }
button {
  cursor: pointer;
  background: #444; color: white; border: 1px solid #666;
  padding: 6px; border-radius: 4px; font-size: 12px;
  transition: all 0.2s;
  text-align: left;
}
button:hover { background: #666; }

/* 3단계 버튼 강조 스타일 */
.btn-danger {
  border-color: #ff5555;
  color: #ffcccc;
}
.btn-danger:hover {
  background: #800000;
}

.status-log { margin-top: 10px; font-size: 11px; color: #ccc; border-top: 1px solid #555; padding-top: 5px;}
</style>