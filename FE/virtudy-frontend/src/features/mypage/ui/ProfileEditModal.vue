<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <button class="close-btn" @click="$emit('close')">X</button>
      
      <div class="avatar-edit-section">
        <div class="avatar-circle">
          <span>아바타<br>수정</span>
        </div>
      </div>

      <div class="form-group">
        <label>이메일</label>
        <div class="readonly-text">{{ email }}</div>
      </div>

      <div class="form-group">
        <label>닉네임</label>
        <input 
          :value="nickName" 
          @input="$emit('update:nickName', ($event.target as HTMLInputElement).value)"
          placeholder="변경할 닉네임을 입력하세요"
        />
      </div>

      <div class="form-group">
        <label>직업</label>
        <select 
          :value="jobType" 
          @change="$emit('update:jobType', ($event.target as HTMLSelectElement).value)"
        >
          <option v-for="opt in jobOptions" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </option>
        </select>
      </div>

      <button class="submit-btn" @click="$emit('submit')">변경하기</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { JOB_OPTIONS } from '../types/mypage.types';

defineProps<{
  email: string;
  nickName: string;
  jobType: string;
  jobOptions: typeof JOB_OPTIONS;
}>();

defineEmits(['close', 'submit', 'update:nickName', 'update:jobType']);
</script>

<style scoped>
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex; justify-content: center; align-items: center;
  z-index: 1000;
}
.modal-content {
  background: #FFF5E0;
  padding: 30px;
  border-radius: 12px;
  width: 400px;
  position: relative;
  border: 2px solid #5A4632;
}
.close-btn {
  position: absolute; top: 10px; right: 15px;
  background: none; border: none; font-size: 1.2rem; cursor: pointer;
}
.avatar-edit-section {
  display: flex; justify-content: center; margin-bottom: 20px;
}
.avatar-circle {
  width: 80px; height: 80px; background: #5A4632; border-radius: 50%;
  color: white; display: flex; align-items: center; justify-content: center;
  cursor: pointer; font-size: 0.8rem; text-align: center;
}
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; color: #8B6E4E; }
.readonly-text { color: #aaa; font-size: 0.9rem; }
input, select {
  width: 100%; padding: 8px; border: 1px solid #D6B48D;
  background: white; border-radius: 4px;
}
.submit-btn {
  width: 100%; padding: 10px; background: #D6B48D; color: white;
  border: none; border-radius: 8px; cursor: pointer; margin-top: 10px;
}
</style>