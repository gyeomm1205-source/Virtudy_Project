<template>
  <div>
    <h1>학습 성향 조사</h1>
    <form @submit.prevent="submitSurvey">
      <!-- 일일 평균 공부 시간 -->
      <div>
        <label for="avg-study-time">일일 평균 공부 시간: </label>
        <select id="avg-study-time" v-model="surveyData.avgStudyTime">
          <option disabled value="">선택하세요</option>
          <option v-for="time in studyTimeOptions" :key="time" :value="time">{{ time }}</option>
        </select>
      </div>

      <!-- 일 목표 공부 시간 -->
      <div>
        <label for="goal-study-time">일 목표 공부 시간: </label>
        <select id="goal-study-time" v-model="surveyData.goalStudyTime">
          <option disabled value="">선택하세요</option>
          <option v-for="time in studyTimeOptions" :key="time" :value="time">{{ time }}</option>
        </select>
      </div>

      <!-- 주 학습 시간대 -->
      <div>
        <p>주 학습 시간대 (중복 가능):</p>
        <label v-for="slot in timeSlots" :key="slot.value">
          <input type="checkbox" :value="slot.value" v-model="surveyData.preferredTimeSlots" />
          {{ slot.text }}
        </label>
      </div>

      <!-- 학습 스타일 -->
      <div>
        <p>학습 스타일:</p>
        <label>
          <input type="radio" value="marathon" v-model="surveyData.studyStyle" />
          마라톤형 (꾸준히 길게)
        </label>
        <label>
          <input type="radio" value="sprint" v-model="surveyData.studyStyle" />
          스프린터형 (짧고 굵게)
        </label>
      </div>

      <!-- 직업(신분) -->
      <div>
        <label for="occupation">직업(신분): </label>
        <select id="occupation" v-model="surveyData.occupation">
          <option disabled value="">선택하세요</option>
          <option v-for="job in jobOptions" :key="job" :value="job">{{ job }}</option>
        </select>
      </div>

      <button type="submit">제출하고 시작하기</button>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import api from '@/api'; // Axios 인스턴스 임포트

const router = useRouter();

const studyTimeOptions = ['1~2시간', '3~4시간', '5~6시간', '7~8시간', '9시간 이상'];
const timeSlots = [
  { text: '새벽 (00:00 ~ 06:00)', value: 'dawn' },
  { text: '오전 (06:00 ~ 12:00)', value: 'morning' },
  { text: '오후 (12:00 ~ 18:00)', value: 'afternoon' },
  { text: '저녁 (18:00 ~ 24:00)', value: 'evening' },
];
const jobOptions = ['학생 (초/중/고)', '대학생/대학원생', '취업준비생', '직장인', '기타'];

const surveyData = ref({
  avgStudyTime: '',
  goalStudyTime: '',
  preferredTimeSlots: [],
  studyStyle: '',
  occupation: '',
});

const submitSurvey = async () => {
  console.log('제출할 설문 데이터:', surveyData.value);
  
  try {
    // 백엔드 API가 준비되었다고 가정하고, 설문 데이터를 전송합니다.
    // 예: await api.post('/v1/members/survey', surveyData.value);
    
    alert('설문이 제출되었습니다. Virtudy를 시작합니다!');
    // 온보딩 완료 후 메인 페이지로 이동
    router.push('/');
  } catch (error) {
    console.error('설문 제출 실패:', error);
    alert('설문 제출 중 오류가 발생했습니다. 다시 시도해주세요.');
  }
};
</script>

<style scoped>
div {
  padding: 20px;
}
form div {
  margin-bottom: 15px;
}
</style>
