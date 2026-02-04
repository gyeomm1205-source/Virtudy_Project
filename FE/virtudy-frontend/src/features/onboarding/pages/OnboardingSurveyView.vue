<template>
  <div class="min-h-screen bg-[var(--color-cream2)]">
    
    <div class="text-center pt-[6rem] pb-[2rem]">
      <h1 class="text-[var(--color-syrup)] text-[2.625rem] font-['Ram'] leading-[3rem] font-medium tracking-[-0.0525rem]">
        학습 성향 조사
      </h1>
    </div>

    <div class="flex flex-col items-center pb-[4rem]">
      <div class="max-w-[50rem] w-full px-[2rem]">
        
        <form @submit.prevent="submitSurvey" class="space-y-[2rem]">
          
          <!-- 일일 평균 공부 시간 -->
          <div class="text-[var(--color-choco)] font-['PfStardust30S'] leading-normal">
            <p class="text-[1.75rem] mb-[0.5rem] font-normal">일일 평균 공부 시간</p>
            <select 
              id="avg-study-time" 
              v-model="surveyData.avgStudyTime"
              class="w-full h-[3.25rem] px-[1rem] text-[1.125rem] text-[var(--color-syrup)] border border-[var(--color-choco)] rounded bg-white font-['PfStardust30S'] focus:outline-none focus:ring-2 focus:ring-[var(--color-choco)]"
            >
              <option disabled value="">선택하세요</option>
              <option v-for="option in studyTimeOptions" :key="option.value" :value="option.value">
                {{ option.text }}
              </option>
            </select>
          </div>

          <!-- 일 목표 공부 시간 -->
          <div class="text-[var(--color-choco)] font-['PfStardust30S'] leading-normal">
            <p class="text-[1.75rem] mb-[0.5rem] font-normal">일 목표 공부 시간</p>
            <select 
              id="goal-study-time" 
              v-model="surveyData.goalStudyTime"
              class="w-full h-[3.25rem] px-[1rem] text-[1.125rem] text-[var(--color-syrup)] border border-[var(--color-choco)] rounded bg-white font-['PfStardust30S'] focus:outline-none focus:ring-2 focus:ring-[var(--color-choco)]"
            >
              <option disabled value="">선택하세요</option>
              <option v-for="option in studyTimeOptions" :key="option.value" :value="option.value">
                {{ option.text }}
              </option>
            </select>
          </div>

          <!-- 주 학습 시간대 -->
          <div class="text-[var(--color-choco)] font-['PfStardust30S'] leading-normal">
            <p class="text-[1.75rem] mb-[1rem] font-normal">주 학습 시간대</p>
            <div class="space-y-[0.75rem]">
              <label v-for="slot in timeSlots" :key="slot.value" class="flex items-center gap-[0.75rem] cursor-pointer select-none">
                <div class="w-[2rem] flex items-center justify-center">
                  <input 
                    type="radio" 
                    :value="slot.value" 
                    v-model="surveyData.preferredTimeSlots" 
                    name="preferredTimeSlotGroup"
                    class="w-[1.125rem] h-[1.125rem] accent-[var(--color-choco)] cursor-pointer"
                  />
                </div>
                <span class="text-[var(--color-choco)] text-[1.125rem] font-['PfStardust30S'] font-normal">
                  {{ slot.text }}
                </span>
              </label>
            </div>
          </div>

          <!-- 학습 스타일 -->
          <div class="text-[var(--color-choco)] font-['PfStardust30S'] leading-normal">
            <p class="text-[1.75rem] mb-[1rem] font-normal">학습 스타일</p>
            <div class="space-y-[0.75rem]">
              <label class="flex items-center gap-[0.75rem] cursor-pointer select-none">
                <div class="w-[2rem] flex items-center justify-center">
                  <input 
                    type="radio" 
                    value="MARATHON" 
                    v-model="surveyData.studyStyle"
                    class="w-[1.125rem] h-[1.125rem] accent-[var(--color-choco)] cursor-pointer"
                  />
                </div>
                <span class="text-[var(--color-choco)] text-[1.125rem] font-['PfStardust30S'] font-normal">
                  마라톤형 (꾸준히 길게)
                </span>
              </label>
              <label class="flex items-center gap-[0.75rem] cursor-pointer select-none">
                <div class="w-[2rem] flex items-center justify-center">
                  <input 
                    type="radio" 
                    value="SPRINTER" 
                    v-model="surveyData.studyStyle"
                    class="w-[1.125rem] h-[1.125rem] accent-[var(--color-choco)] cursor-pointer"
                  />
                </div>
                <span class="text-[var(--color-choco)] text-[1.125rem] font-['PfStardust30S'] font-normal">
                  스프린터형 (짧고 굵게)
                </span>
              </label>
            </div>
          </div>

          <!-- 직업(신분) -->
          <div class="text-[var(--color-choco)] font-['PfStardust30S'] leading-normal">
            <p class="text-[1.75rem] mb-[0.5rem] font-normal">직업(신분)</p>
            <select 
              id="occupation" 
              v-model="surveyData.occupation"
              class="w-full h-[3.25rem] px-[1rem] text-[1.125rem] text-[var(--color-syrup)] border border-[var(--color-choco)] rounded bg-white font-['PfStardust30S'] focus:outline-none focus:ring-2 focus:ring-[var(--color-choco)]"
            >
              <option disabled value="">선택하세요</option>
              <option v-for="option in jobOptions" :key="option.value" :value="option.value">
                {{ option.text }}
              </option>
            </select>
          </div>

          <div class="flex justify-center pt-[2rem]">
            <button 
              type="submit"
              :disabled="!canSubmit"
              class="bg-[var(--color-butter)] border-2 border-[var(--color-choco)] px-[2rem] py-[1.25rem] rounded-[0.125rem] font-['Xcu'] text-[1.75rem] text-[var(--color-choco)] font-medium leading-none transition-all duration-200"
              :class="{
                'opacity-50 cursor-not-allowed': !canSubmit,
                'hover:scale-105 active:scale-95 cursor-pointer': canSubmit
              }"
              style="box-shadow: 4px 4px 0px 0px var(--color-choco);"
            >
              제출하기
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import api from '@/shared/api/axios.config.ts';

const router = useRouter();
const authStore = useAuthStore();

// [수정 3] 보여주는 텍스트(text)와 서버로 보낼 코드(value) 정의
// 백엔드 Enum과 철자가 정확히 일치해야 합니다.
const studyTimeOptions = [
  { text: '1~2시간', value: 'ONE_TO_TWO' },
  { text: '3~4시간', value: 'THREE_TO_FOUR' },
  { text: '5~6시간', value: 'FIVE_TO_SIX' },
  { text: '7~8시간', value: 'SEVEN_TO_EIGHT' },
  { text: '9시간 이상', value: 'OVER_NINE' },
];

const timeSlots = [
  { text: '새벽 (00:00 ~ 06:00)', value: 'DAWN' },
  { text: '오전 (06:00 ~ 12:00)', value: 'MORNING' },
  { text: '오후 (12:00 ~ 18:00)', value: 'AFTERNOON' },
  { text: '저녁 (18:00 ~ 24:00)', value: 'EVENING' },
];

const jobOptions = [
  { text: '학생 (초/중/고)', value: 'SCHOOL_STUDENT' },
  { text: '대학생/대학원생', value: 'UNIVERSITY_STUDENT' },
  { text: '취업준비생', value: 'JOB_SEEKER' },
  { text: '직장인', value: 'OFFICE_WORKER' },
];

interface SurveyData {
  avgStudyTime: string;
  goalStudyTime: string;
  preferredTimeSlots: string;
  studyStyle: string;
  occupation: string;
}

const surveyData = ref<SurveyData>({
  avgStudyTime: '',
  goalStudyTime: '',
  preferredTimeSlots: '', //빈 문자열로 초기화
  studyStyle: '', // 초기값 빈 문자열
  occupation: '',
});

const canSubmit = computed(() => (
  !!surveyData.value.avgStudyTime &&
  !!surveyData.value.goalStudyTime &&
  !!surveyData.value.preferredTimeSlots &&
  !!surveyData.value.studyStyle &&
  !!surveyData.value.occupation
));

const submitSurvey = async () => {
  let signupInfo = authStore.signupInfo;

  if (!signupInfo) {
    const storedInfo = localStorage.getItem('signupInfo');
    if (storedInfo && storedInfo !== "undefined") {
      try {
        signupInfo = JSON.parse(storedInfo);
        authStore.setSignupInfo(signupInfo);
      } catch (e) {
        console.error('가입 정보 파싱 실패', e);
      }
    }
  }

  if (!signupInfo) {
    alert('비정상적인 접근입니다. 가입 정보가 유실되었습니다.');
    router.push({ name: 'guest' });
    return;
  }

  try {
    // Client-side validation
    if (
      !surveyData.value.avgStudyTime ||
      !surveyData.value.goalStudyTime ||
      !surveyData.value.preferredTimeSlots || // 변경: 빈 문자열 체크
      !surveyData.value.studyStyle ||
      !surveyData.value.occupation
    ) {
      alert('모든 항목을 선택해주세요.');
      return;
    }

    // [수정 4] 복잡한 변환 로직 제거하고 바로 전송
    const payload = {
      // 1. Auth Info
      email: signupInfo.email.trim(),
      nickname: signupInfo.tempNickname,
      isServiceAgreed: signupInfo.isServiceAgreed,
      isVideoAgreed: signupInfo.isVideoAgreed,
      isPersonaAgreed: signupInfo.isPersonaAgreed,

      // 2. Survey Info 
      studyType: surveyData.value.studyStyle, // "MARATHON" or "SPRINT"
      activeTime: surveyData.value.preferredTimeSlots, // 단일 문자열 전송
      jobType: surveyData.value.occupation, // "UNIVERSITY_STUDENT" etc.
      
      averageHours: surveyData.value.avgStudyTime,   // "ONE_TO_TWO"
      targetHours: surveyData.value.goalStudyTime, // "ONE_TO_TWO"
    };

    console.log('전송할 데이터:', payload);
    const response = await api.post('/auth/signup', payload);

    const { accessToken } = response.data;
    authStore.setToken(accessToken);

    authStore.clearSignupInfo();
    localStorage.removeItem('signupInfo');

    alert('회원가입이 완료되었습니다. Virtudy를 시작합니다!');
    router.push({ name: 'user' });

  } catch (error) {
    console.error('최종 회원가입 실패:', error);
    alert('회원가입 중 오류가 발생했습니다. 다시 시도해주세요.');
  }
};
</script>

<style scoped>
/* 기본 스타일은 Tailwind CSS 클래스로 처리 */
</style>