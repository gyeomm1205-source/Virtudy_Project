<!-- 테스트용 버튼을 모아둔 컴포넌트입니다. -->

<template>
  <div class="debug-panel">
    <h3>🛠️ 개발자 테스트 패널</h3>
    <div class="btn-group">
      <button @click="addDummyMember">1. 가짜 팀원 입장 (80점)</button>
      <button @click="makeDummyDrowsy">2. 팀원 점수 깎기 (30점)</button>
      <button @click="triggerSelfStun">3. 😵 내가 섬광탄 맞기</button>
      <button @click="reset">🔄 초기화</button>
    </div>
    <div class="status-log">
      <p>팀원 수: {{ Object.keys(scores).length }}명</p>
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

// 1. 가짜 팀원 추가 (점수 80점 - 깨우기 버튼 비활성 상태 테스트)
const addDummyMember = () => {
  // 부모의 ref 객체에 직접 주입
  props.names[DUMMY_ID] = '졸린 개발자';
  props.scores[DUMMY_ID] = 80; 
  console.log('🧪 가짜 팀원 입장 완료');
};

// 2. 점수 깎기 (깨우기 버튼 활성화 테스트)
const makeDummyDrowsy = () => {
  if (props.scores[DUMMY_ID] !== undefined) {
    props.scores[DUMMY_ID] = 30; // 60점 미만으로 변경
    console.log('🧪 팀원 점수 30점으로 하락 -> 깨우기 버튼 활성화 되어야 함');
  } else {
    alert('먼저 가짜 팀원을 입장시키세요!');
  }
};

// 3. 내가 맞기 (PIP 해제 및 이펙트 테스트)
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
  z-index: 10000; /* 최상단 */
  color: white;
  font-family: 'PfStardust30S';
  width: 250px;
}
h3 { margin: 0 0 10px 0; font-size: 14px; color: #ff5555; }
.btn-group { display: flex; flex-direction: column; gap: 5px; }
button {
  cursor: pointer;
  background: #444; color: white; border: 1px solid #666;
  padding: 5px; border-radius: 4px; font-size: 12px;
}
button:hover { background: #666; }
.status-log { margin-top: 10px; font-size: 11px; color: #ccc; }
</style>