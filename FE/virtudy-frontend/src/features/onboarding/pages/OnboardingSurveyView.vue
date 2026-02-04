<template>
  <div class="min-h-screen bg-[var(--color-cream2)]">
    <div class="text-center pt-[4.5rem] pb-[1.5rem]">
      <h1 class="text-[var(--color-syrup)] text-[2.625rem] sm:text-[3rem] font-['Ram'] leading-[3rem] font-medium tracking-[-0.0525rem]">
        학습 성향 조사
      </h1>
      <p class="mt-[0.5rem] text-[0.95rem] sm:text-[1rem] text-[var(--color-choco)] font-['PfStardust30S']">
        카드 뉴스처럼 한 단계씩 선택해 주세요
      </p>
    </div>

    <div class="flex flex-col items-center pb-[4rem]">
      <div class="max-w-[46rem] w-full px-[1.25rem] sm:px-[2rem]">
        <div class="flex flex-col items-center mb-[1.5rem]">
          <div class="flex items-center gap-[0.75rem] mb-[0.5rem]">
            <span class="text-[0.95rem] text-[var(--color-choco)] font-['PfStardust30S']">
              {{ currentStep }}단계
            </span>
            <span class="text-[0.9rem] text-[var(--color-choco)]/70 font-['PfStardust30S']">
              5단계
            </span>
          </div>
          <div class="relative w-full max-w-[28rem]">
            <div class="h-[0.75rem] rounded-full bg-white border-2 border-[var(--color-choco)] overflow-hidden shadow-[3px_3px_0px_0px_var(--color-choco)]"></div>
            <div
              class="absolute left-0 top-0 h-[0.75rem] rounded-full bg-[var(--color-butter)] transition-all duration-300"
              :style="{ width: progressWidth }"
            ></div>
            <div class="mt-[0.5rem] flex justify-between text-[0.8rem] text-[var(--color-choco)] font-['PfStardust30S']">
              <span>1단계</span>
              <span>2단계</span>
              <span>3단계</span>
              <span>4단계</span>
              <span>5단계</span>
            </div>
          </div>
        </div>

        <form @submit.prevent="submitSurvey">
          <div v-if="isSubmitted" class="flex flex-col items-center justify-center min-h-[26rem]">
            <p class="text-[1.5rem] sm:text-[1.75rem] text-[var(--color-choco)] font-['PfStardust30S']">
              성공적으로 제출되었습니다.
            </p>
            <p class="mt-[0.5rem] text-[0.95rem] text-[var(--color-choco)]/70 font-['PfStardust30S']">
              잠시 후 페이지로 이동합니다.
            </p>
          </div>
          <div v-else-if="isSubmitting" class="flex flex-col items-center justify-center min-h-[26rem]">
            <p class="text-[1.5rem] sm:text-[1.75rem] text-[var(--color-choco)] font-['PfStardust30S']">
              제출 중입니다...
            </p>
            <p class="mt-[0.5rem] text-[0.95rem] text-[var(--color-choco)]/70 font-['PfStardust30S']">
              잠시만 기다려주세요.
            </p>
          </div>
          <Transition v-else name="slide" mode="out-in">
            <section
              v-if="currentStep === 1"
              key="step-1"
              class="bg-white border-2 border-[var(--color-choco)] rounded-[0.75rem] p-[1.5rem] sm:p-[2rem] shadow-[6px_6px_0px_0px_var(--color-choco)] min-h-[26rem] flex flex-col"
            >
              <p class="text-[1.5rem] sm:text-[1.75rem] mb-[0.35rem] text-[var(--color-choco)] font-['PfStardust30S']">일 평균 공부 시간</p>
              <p class="text-[0.9rem] text-[var(--color-choco)]/70 font-['PfStardust30S'] mb-[1rem]">
                아래에서 하나를 선택하면 다음 단계로 넘어가요.
              </p>
              <div class="grid sm:grid-cols-2 gap-[0.75rem]">
                <label v-for="option in studyTimeOptions" :key="option.value" class="flex items-center gap-[0.75rem] cursor-pointer select-none border border-[var(--color-choco)] rounded-[0.5rem] px-[1rem] py-[0.75rem]">
                  <input
                    type="radio"
                    :value="option.value"
                    v-model="surveyData.avgStudyTime"
                    name="avgStudyTimeGroup"
                    class="w-[1.125rem] h-[1.125rem] accent-[var(--color-choco)] cursor-pointer"
                    @change="goNextFromAvg"
                  />
                  <span class="text-[var(--color-choco)] text-[1.05rem] font-['PfStardust30S']">
                    {{ option.text }}
                  </span>
                </label>
              </div>
              <div class="mt-[1rem] text-[0.9rem] text-[var(--color-choco)]/80 font-['PfStardust30S']">
                선택됨: <span class="font-medium text-[var(--color-choco)]">{{ findLabel(studyTimeOptions, surveyData.avgStudyTime) }}</span>
              </div>
              <div class="mt-auto pt-[1.25rem] flex justify-end">
                <button
                  type="button"
                  class="text-[var(--color-choco)] font-['PfStardust30S'] underline opacity-80"
                  @click="goPrev"
                >
                  뒤로가기
                </button>
              </div>
            </section>

            <section
              v-else-if="currentStep === 2"
              key="step-2"
              class="bg-white border-2 border-[var(--color-choco)] rounded-[0.75rem] p-[1.5rem] sm:p-[2rem] shadow-[6px_6px_0px_0px_var(--color-choco)] min-h-[26rem] flex flex-col"
            >
              <p class="text-[1.5rem] sm:text-[1.75rem] mb-[0.35rem] text-[var(--color-choco)] font-['PfStardust30S']">일 목표 공부 시간</p>
              <p class="text-[0.9rem] text-[var(--color-choco)]/70 font-['PfStardust30S'] mb-[1rem]">
                목표를 정하면 맞춤 추천에 도움이 돼요.
              </p>
              <div class="grid sm:grid-cols-2 gap-[0.75rem]">
                <label v-for="option in studyTimeOptions" :key="option.value" class="flex items-center gap-[0.75rem] cursor-pointer select-none border border-[var(--color-choco)] rounded-[0.5rem] px-[1rem] py-[0.75rem]">
                  <input
                    type="radio"
                    :value="option.value"
                    v-model="surveyData.goalStudyTime"
                    name="goalStudyTimeGroup"
                    class="w-[1.125rem] h-[1.125rem] accent-[var(--color-choco)] cursor-pointer"
                    @change="goNextFromGoal"
                  />
                  <span class="text-[var(--color-choco)] text-[1.05rem] font-['PfStardust30S']">
                    {{ option.text }}
                  </span>
                </label>
              </div>
              <div class="mt-[1rem] text-[0.9rem] text-[var(--color-choco)]/80 font-['PfStardust30S']">
                선택됨: <span class="font-medium text-[var(--color-choco)]">{{ findLabel(studyTimeOptions, surveyData.goalStudyTime) }}</span>
              </div>
              <div class="mt-auto pt-[1.25rem] flex justify-end">
                <button
                  type="button"
                  class="text-[var(--color-choco)] font-['PfStardust30S'] underline opacity-80"
                  @click="goPrev"
                >
                  뒤로가기
                </button>
              </div>
            </section>

            <section
              v-else-if="currentStep === 3"
              key="step-3"
              class="bg-white border-2 border-[var(--color-choco)] rounded-[0.75rem] p-[1.5rem] sm:p-[2rem] shadow-[6px_6px_0px_0px_var(--color-choco)] min-h-[26rem] flex flex-col"
            >
              <p class="text-[1.5rem] sm:text-[1.75rem] mb-[0.35rem] text-[var(--color-choco)] font-['PfStardust30S']">주 학습 시간대</p>
              <p class="text-[0.9rem] text-[var(--color-choco)]/70 font-['PfStardust30S'] mb-[1rem]">
                가장 집중이 잘 되는 시간대를 골라주세요.
              </p>
              <div class="grid sm:grid-cols-2 gap-[0.75rem]">
                <label v-for="slot in timeSlots" :key="slot.value" class="flex items-center gap-[0.75rem] cursor-pointer select-none border border-[var(--color-choco)] rounded-[0.5rem] px-[1rem] py-[0.75rem]">
                  <input
                    type="radio"
                    :value="slot.value"
                    v-model="surveyData.preferredTimeSlots"
                    name="preferredTimeSlotGroup"
                    class="w-[1.125rem] h-[1.125rem] accent-[var(--color-choco)] cursor-pointer"
                    @change="goNextFromTime"
                  />
                  <span class="text-[var(--color-choco)] text-[1.05rem] font-['PfStardust30S']">
                    {{ slot.text }}
                  </span>
                </label>
              </div>
              <div class="mt-[1rem] text-[0.9rem] text-[var(--color-choco)]/80 font-['PfStardust30S']">
                선택됨: <span class="font-medium text-[var(--color-choco)]">{{ findLabel(timeSlots, surveyData.preferredTimeSlots) }}</span>
              </div>
              <div class="mt-auto pt-[1.25rem] flex justify-end">
                <button
                  type="button"
                  class="text-[var(--color-choco)] font-['PfStardust30S'] underline opacity-80"
                  @click="goPrev"
                >
                  뒤로가기
                </button>
              </div>
            </section>

            <section
              v-else-if="currentStep === 4"
              key="step-4"
              class="bg-white border-2 border-[var(--color-choco)] rounded-[0.75rem] p-[1.5rem] sm:p-[2rem] shadow-[6px_6px_0px_0px_var(--color-choco)] min-h-[26rem] flex flex-col"
            >
              <p class="text-[1.5rem] sm:text-[1.75rem] mb-[0.35rem] text-[var(--color-choco)] font-['PfStardust30S']">학습 스타일</p>
              <p class="text-[0.9rem] text-[var(--color-choco)]/70 font-['PfStardust30S'] mb-[1rem]">
                학습 루틴에 맞춘 추천을 제공해요.
              </p>
              <div class="grid sm:grid-cols-2 gap-[0.75rem]">
                <label class="flex items-center gap-[0.75rem] cursor-pointer select-none border border-[var(--color-choco)] rounded-[0.5rem] px-[1rem] py-[0.75rem]">
                  <input
                    type="radio"
                    value="MARATHON"
                    v-model="surveyData.studyStyle"
                    name="studyStyleGroup"
                    class="w-[1.125rem] h-[1.125rem] accent-[var(--color-choco)] cursor-pointer"
                    @change="goNextFromStyle"
                  />
                  <span class="text-[var(--color-choco)] text-[1.05rem] font-['PfStardust30S']">
                    마라톤형 (꾸준히 길게)
                  </span>
                </label>
                <label class="flex items-center gap-[0.75rem] cursor-pointer select-none border border-[var(--color-choco)] rounded-[0.5rem] px-[1rem] py-[0.75rem]">
                  <input
                    type="radio"
                    value="SPRINTER"
                    v-model="surveyData.studyStyle"
                    name="studyStyleGroup"
                    class="w-[1.125rem] h-[1.125rem] accent-[var(--color-choco)] cursor-pointer"
                    @change="goNextFromStyle"
                  />
                  <span class="text-[var(--color-choco)] text-[1.05rem] font-['PfStardust30S']">
                    스프린터형 (짧고 굵게)
                  </span>
                </label>
              </div>
              <div class="mt-[1rem] text-[0.9rem] text-[var(--color-choco)]/80 font-['PfStardust30S']">
                선택됨: <span class="font-medium text-[var(--color-choco)]">{{ surveyData.studyStyle ? (surveyData.studyStyle === 'MARATHON' ? '마라톤형 (꾸준히 길게)' : '스프린터형 (짧고 굵게)') : '아직 선택되지 않았어요' }}</span>
              </div>
              <div class="mt-auto pt-[1.25rem] flex justify-end">
                <button
                  type="button"
                  class="text-[var(--color-choco)] font-['PfStardust30S'] underline opacity-80"
                  @click="goPrev"
                >
                  뒤로가기
                </button>
              </div>
            </section>

            <section
              v-else-if="currentStep === 5"
              key="step-5"
              class="bg-white border-2 border-[var(--color-choco)] rounded-[0.75rem] p-[1.5rem] sm:p-[2rem] shadow-[6px_6px_0px_0px_var(--color-choco)] min-h-[26rem] flex flex-col"
            >
              <p class="text-[1.5rem] sm:text-[1.75rem] mb-[0.35rem] text-[var(--color-choco)] font-['PfStardust30S']">직업(신분)</p>
              <p class="text-[0.9rem] text-[var(--color-choco)]/70 font-['PfStardust30S'] mb-[1rem]">
                직업 정보로 추천 콘텐츠가 달라져요.
              </p>
              <div class="grid sm:grid-cols-2 gap-[0.75rem]">
                <label v-for="option in jobOptions" :key="option.value" class="flex items-center gap-[0.75rem] cursor-pointer select-none border border-[var(--color-choco)] rounded-[0.5rem] px-[1rem] py-[0.75rem]">
                  <input
                    type="radio"
                    :value="option.value"
                    v-model="surveyData.occupation"
                    name="occupationGroup"
                    class="w-[1.125rem] h-[1.125rem] accent-[var(--color-choco)] cursor-pointer"
                    @change="goNextFromJob"
                  />
                  <span class="text-[var(--color-choco)] text-[1.05rem] font-['PfStardust30S']">
                    {{ option.text }}
                  </span>
                </label>
              </div>
              <div class="mt-[1rem] text-[0.9rem] text-[var(--color-choco)]/80 font-['PfStardust30S']">
                선택됨: <span class="font-medium text-[var(--color-choco)]">{{ findLabel(jobOptions, surveyData.occupation) }}</span>
              </div>
              <div class="mt-auto pt-[1.25rem] flex justify-end">
                <button
                  type="button"
                  class="text-[var(--color-choco)] font-['PfStardust30S'] underline opacity-80"
                  @click="goPrev"
                >
                  뒤로가기
                </button>
              </div>
            </section>
          </Transition>
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

type OptionItem = { text: string; value: string };

const findLabel = (options: OptionItem[], value: string) =>
  options.find((option) => option.value === value)?.text ?? '아직 선택되지 않았어요';

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

const currentStep = ref(1);
const progressWidth = computed(() => `${Math.min(currentStep.value, 5) * (100 / 5)}%`);
const isSubmitting = ref(false);
const isSubmitted = ref(false);

const goToStep = (step: number) => {
  currentStep.value = Math.min(Math.max(step, 1), 5);
};

const goPrev = () => {
  goToStep(currentStep.value - 1);
};

const goNextFromAvg = () => {
  if (surveyData.value.avgStudyTime) goToStep(2);
};

const goNextFromGoal = () => {
  if (surveyData.value.goalStudyTime) goToStep(3);
};

const goNextFromTime = () => {
  if (surveyData.value.preferredTimeSlots) goToStep(4);
};

const goNextFromStyle = () => {
  if (surveyData.value.studyStyle) goToStep(5);
};

const goNextFromJob = () => {
  if (!surveyData.value.occupation || isSubmitting.value) return;
  submitSurvey();
};

const canSubmit = computed(() => (
  !!surveyData.value.avgStudyTime &&
  !!surveyData.value.goalStudyTime &&
  !!surveyData.value.preferredTimeSlots &&
  !!surveyData.value.studyStyle &&
  !!surveyData.value.occupation
));

const submitSurvey = async () => {
  if (isSubmitting.value) return;
  isSubmitting.value = true;
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
    isSubmitting.value = false;
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
      isSubmitting.value = false;
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
    isSubmitted.value = true;
    isSubmitting.value = false;
    setTimeout(() => {
      router.push({ name: 'user' });
    }, 1200);

  } catch (error) {
    console.error('최종 회원가입 실패:', error);
    alert('회원가입 중 오류가 발생했습니다. 다시 시도해주세요.');
    isSubmitting.value = false;
  }
};
</script>

<style scoped>
.slide-enter-active,
.slide-leave-active {
  transition: all 0.35s ease;
}

.slide-enter-from {
  opacity: 0;
  transform: translateX(40px);
}

.slide-leave-to {
  opacity: 0;
  transform: translateX(-40px);
}
</style>