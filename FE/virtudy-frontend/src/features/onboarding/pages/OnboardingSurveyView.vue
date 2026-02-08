<template>
  <div 
    class="min-h-screen"
    :style="{ 
      backgroundImage: `url(${bgBrick})`,
      backgroundRepeat: 'repeat' 
    }"
  >
    <div class="flex flex-col items-center pt-[7rem] pb-[4rem] px-[1rem]">
      
      <div class="max-w-[46rem] w-full mx-[1.25rem] sm:mx-[2rem] bg-white border-2 border-[var(--color-choco)] rounded-[0.75rem] p-[1.5rem] sm:p-[3rem] shadow-[6px_6px_0px_0px_var(--color-choco)]">
        
        <div v-if="currentStep <= 5" class="text-center mb-[2.5rem]">
          <h1 class="text-[var(--color-syrup)] text-[2.625rem] sm:text-[3rem] font-['Ram'] leading-[3rem] font-medium tracking-[-0.0525rem]">
            학습 성향 조사
          </h1>
          <p class="mt-[0.5rem] text-[0.95rem] sm:text-[1rem] text-[var(--color-choco)] font-['PfStardust30S']">
            한 단계씩 선택해 주세요!
          </p>
        </div>

        <div v-if="currentStep <= 5" class="flex flex-col items-center mb-[2.5rem]">
          <div class="relative w-full max-w-[28rem]">
            <div class="h-[0.75rem] rounded-full bg-[var(--color-cream2)] border-2 border-[var(--color-choco)] overflow-hidden shadow-[2px_2px_0px_0px_var(--color-choco)]"></div>
            
            <div class="absolute -top-[1.5rem] right-0 flex items-center gap-[0.5rem]">
              <span class="text-[0.95rem] text-[var(--color-choco)] font-['PfStardust30S']">
                {{ currentStep }}단계
              </span>
              <span class="text-[0.9rem] text-[var(--color-choco)]/50 font-['PfStardust30S']">
                / 5단계
              </span>
            </div>

            <div
              class="absolute left-0 top-0 h-[0.75rem] rounded-full bg-[var(--color-butter)] border-r-2 border-[var(--color-choco)] transition-all duration-300"
              :style="{ width: progressWidth }"
            ></div>
            
            <div class="mt-[0.5rem] flex justify-between text-[0.8rem] text-[var(--color-choco)]/80 font-['PfStardust30S']">
              <span>1단계</span>
              <span>2단계</span>
              <span>3단계</span>
              <span>4단계</span>
              <span>5단계</span>
            </div>
          </div>
        </div>

        <form @submit.prevent="submitSurvey">
            <div v-if="isSubmitted" class="flex flex-col items-center justify-center min-h-[18rem]">
              <p class="text-[1.5rem] sm:text-[1.75rem] text-[var(--color-choco)] font-['PfStardust30S']">
                성공적으로 제출되었습니다.
              </p>
              <p class="mt-[0.5rem] text-[0.95rem] text-[var(--color-choco)]/70 font-['PfStardust30S']">
                잠시 후 페이지로 이동합니다.
              </p>
            </div>
            <div v-else-if="isSubmitting" class="flex flex-col items-center justify-center min-h-[18rem]">
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
                  class="flex flex-col min-h-[18rem]"
                >
                   <p class="text-[1.5rem] sm:text-[1.75rem] mb-[0.35rem] text-[var(--color-choco)] font-['PfStardust30S']">일 평균 공부 시간</p>
                  <p class="text-[0.9rem] text-[var(--color-choco)]/70 font-['PfStardust30S'] mb-[1.5rem]">
                    아래에서 하나를 선택하면 다음 단계로 넘어가요.
                  </p>
                  <div class="grid sm:grid-cols-2 gap-[0.75rem]">
                    <label v-for="option in studyTimeOptions" :key="option.value" class="flex items-center gap-[0.75rem] cursor-pointer select-none border border-[var(--color-choco)] rounded-[0.5rem] px-[1rem] py-[0.75rem] hover:bg-[var(--color-cream2)] transition-colors">
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
                    <button type="button" class="text-[var(--color-choco)] font-['PfStardust30S'] underline opacity-80" @click="goPrev">뒤로가기</button>
                  </div>
                </section>

                <section v-else-if="currentStep === 2" key="step-2" class="flex flex-col min-h-[18rem]">
                    <p class="text-[1.5rem] sm:text-[1.75rem] mb-[0.35rem] text-[var(--color-choco)] font-['PfStardust30S']">일 목표 공부 시간</p>
                    <p class="text-[0.9rem] text-[var(--color-choco)]/70 font-['PfStardust30S'] mb-[1.5rem]">목표를 정하면 맞춤 추천에 도움이 돼요.</p>
                    <div class="grid sm:grid-cols-2 gap-[0.75rem]">
                      <label v-for="option in studyTimeOptions" :key="option.value" class="flex items-center gap-[0.75rem] cursor-pointer select-none border border-[var(--color-choco)] rounded-[0.5rem] px-[1rem] py-[0.75rem] hover:bg-[var(--color-cream2)] transition-colors">
                        <input type="radio" :value="option.value" v-model="surveyData.goalStudyTime" name="goalStudyTimeGroup" class="w-[1.125rem] h-[1.125rem] accent-[var(--color-choco)] cursor-pointer" @change="goNextFromGoal" />
                        <span class="text-[var(--color-choco)] text-[1.05rem] font-['PfStardust30S']">{{ option.text }}</span>
                      </label>
                    </div>
                    <div class="mt-auto pt-[1.25rem] flex justify-end"><button type="button" class="text-[var(--color-choco)] font-['PfStardust30S'] underline opacity-80" @click="goPrev">뒤로가기</button></div>
                </section>

                <section v-else-if="currentStep === 3" key="step-3" class="flex flex-col min-h-[18rem]">
                    <p class="text-[1.5rem] sm:text-[1.75rem] mb-[0.35rem] text-[var(--color-choco)] font-['PfStardust30S']">주 학습 시간대</p>
                    <p class="text-[0.9rem] text-[var(--color-choco)]/70 font-['PfStardust30S'] mb-[1.5rem]">가장 집중이 잘 되는 시간대를 골라주세요.</p>
                    <div class="grid sm:grid-cols-2 gap-[0.75rem]">
                      <label v-for="slot in timeSlots" :key="slot.value" class="flex items-center gap-[0.75rem] cursor-pointer select-none border border-[var(--color-choco)] rounded-[0.5rem] px-[1rem] py-[0.75rem] hover:bg-[var(--color-cream2)] transition-colors">
                        <input type="radio" :value="slot.value" v-model="surveyData.preferredTimeSlots" name="preferredTimeSlotGroup" class="w-[1.125rem] h-[1.125rem] accent-[var(--color-choco)] cursor-pointer" @change="goNextFromTime" />
                        <span class="text-[var(--color-choco)] text-[1.05rem] font-['PfStardust30S']">{{ slot.text }}</span>
                      </label>
                    </div>
                    <div class="mt-auto pt-[1.25rem] flex justify-end"><button type="button" class="text-[var(--color-choco)] font-['PfStardust30S'] underline opacity-80" @click="goPrev">뒤로가기</button></div>
                </section>

                <section v-else-if="currentStep === 4" key="step-4" class="flex flex-col min-h-[18rem]">
                    <p class="text-[1.5rem] sm:text-[1.75rem] mb-[0.35rem] text-[var(--color-choco)] font-['PfStardust30S']">학습 스타일</p>
                    <p class="text-[0.9rem] text-[var(--color-choco)]/70 font-['PfStardust30S'] mb-[1.5rem]">학습 루틴에 맞춘 추천을 제공해요.</p>
                    <div class="grid sm:grid-cols-2 gap-[0.75rem]">
                      <label class="flex items-center gap-[0.75rem] cursor-pointer select-none border border-[var(--color-choco)] rounded-[0.5rem] px-[1rem] py-[0.75rem] hover:bg-[var(--color-cream2)] transition-colors">
                        <input type="radio" value="MARATHON" v-model="surveyData.studyStyle" name="studyStyleGroup" class="w-[1.125rem] h-[1.125rem] accent-[var(--color-choco)] cursor-pointer" @change="goNextFromStyle" />
                        <span class="text-[var(--color-choco)] text-[1.05rem] font-['PfStardust30S']">마라톤형 (꾸준히 길게)</span>
                      </label>
                      <label class="flex items-center gap-[0.75rem] cursor-pointer select-none border border-[var(--color-choco)] rounded-[0.5rem] px-[1rem] py-[0.75rem] hover:bg-[var(--color-cream2)] transition-colors">
                        <input type="radio" value="SPRINTER" v-model="surveyData.studyStyle" name="studyStyleGroup" class="w-[1.125rem] h-[1.125rem] accent-[var(--color-choco)] cursor-pointer" @change="goNextFromStyle" />
                        <span class="text-[var(--color-choco)] text-[1.05rem] font-['PfStardust30S']">스프린터형 (짧고 굵게)</span>
                      </label>
                    </div>
                    <div class="mt-auto pt-[1.25rem] flex justify-end"><button type="button" class="text-[var(--color-choco)] font-['PfStardust30S'] underline opacity-80" @click="goPrev">뒤로가기</button></div>
                </section>

                <section v-else-if="currentStep === 5" key="step-5" class="flex flex-col min-h-[18rem]">
                    <p class="text-[1.5rem] sm:text-[1.75rem] mb-[0.35rem] text-[var(--color-choco)] font-['PfStardust30S']">직업(신분)</p>
                    <p class="text-[0.9rem] text-[var(--color-choco)]/70 font-['PfStardust30S'] mb-[1.5rem]">직업 정보로 추천 콘텐츠가 달라져요.</p>
                    <div class="grid sm:grid-cols-2 gap-[0.75rem]">
                      <label v-for="option in jobOptions" :key="option.value" class="flex items-center gap-[0.75rem] cursor-pointer select-none border border-[var(--color-choco)] rounded-[0.5rem] px-[1rem] py-[0.75rem] hover:bg-[var(--color-cream2)] transition-colors">
                        <input type="radio" :value="option.value" v-model="surveyData.occupation" name="occupationGroup" class="w-[1.125rem] h-[1.125rem] accent-[var(--color-choco)] cursor-pointer" @change="goNextFromJob" />
                        <span class="text-[var(--color-choco)] text-[1.05rem] font-['PfStardust30S']">{{ option.text }}</span>
                      </label>
                    </div>
                    <div class="mt-auto pt-[1.25rem] flex justify-end"><button type="button" class="text-[var(--color-choco)] font-['PfStardust30S'] underline opacity-80" @click="goPrev">뒤로가기</button></div>
                </section>
                
                <section v-else-if="currentStep === 6" key="step-6" class="flex flex-col min-h-[18rem] items-center text-center">
                  <h1 class="text-[var(--color-jam)] text-[2.625rem] sm:text-[3rem] font-['Ram'] leading-[3rem] font-medium tracking-[-0.0525rem] mb-[2.5rem]">
                    주의사항
                  </h1>
                  
                  <div class="bg-[var(--color-cream)] border border-[var(--color-choco)] rounded-[0.5rem] p-[1.5rem] mb-[2.5rem] w-full max-w-[24rem]">
                    <p class="text-[1.1rem] text-[var(--color-choco)] font-['PfStardust30S'] leading-relaxed break-keep">
                      해당 플랫폼의 일부 기능<span class="text-[var(--color-syrup)]">(섬광탄)</span>은<br>
                      <span class="text-[var(--color-jam)] font-bold">광과민성 반응</span>을 유발할 수 있습니다.
                    </p>
                  </div>

                  <button 
                    type="button" 
                    class="bg-[var(--color-butter)] border-2 border-[var(--color-choco)] rounded-[0.5rem] px-[2rem] py-[0.75rem] shadow-[4px_4px_0px_0px_var(--color-choco)] active:shadow-[2px_2px_0px_0px_var(--color-choco)] active:translate-y-[2px] transition-all"
                    @click="submitSurvey"
                  >
                    <span class="text-[var(--color-choco)] font-['PfStardust30S'] text-[1.2rem] font-bold">
                      확인했습니다
                    </span>
                  </button>
                  
                  <div class="mt-auto pt-[1.25rem] w-full flex justify-end">
                    <button type="button" class="text-[var(--color-choco)] font-['PfStardust30S'] underline opacity-80" @click="goPrev">뒤로가기</button>
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
import { useUiStore } from '@/stores/uiStore';
import api from '@/shared/api/axios.config.ts';
import bgBrick from '@/assets/bg_brick.png';

const router = useRouter();
const authStore = useAuthStore();
const uiStore = useUiStore();

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
  preferredTimeSlots: '',
  studyStyle: '',
  occupation: '',
});

const currentStep = ref(1);
// 변경 5: 5단계 기준으로 퍼센트 계산 (6단계가 되어도 100%를 넘지 않도록 min 처리)
const progressWidth = computed(() => `${Math.min(currentStep.value, 5) * (100 / 5)}%`);
const isSubmitting = ref(false);
const isSubmitted = ref(false);

const goToStep = (step: number) => {
  // 최대 6단계 (내부적으로는 6단계가 주의사항 뷰)
  currentStep.value = Math.min(Math.max(step, 1), 6);
};

// 뒤로가기 로직
const goPrev = () => {
  if (currentStep.value === 1) {
    // 1단계에서는 약관 동의 페이지로 이동
    router.push({ name: 'terms' }); 
    return;
  }
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
  if (!surveyData.value.occupation) return;
  // 5단계 선택 완료 시 6단계(주의사항 화면)로 이동
  goToStep(6);
};

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
    await uiStore.openAlert(
      '비정상적인 접근입니다.\n가입 정보가 유실되었습니다.', 
      '오류'
    );
    router.push({ name: 'guest' });
    isSubmitting.value = false;
    return;
  }

  try {
    if (
      !surveyData.value.avgStudyTime ||
      !surveyData.value.goalStudyTime ||
      !surveyData.value.preferredTimeSlots ||
      !surveyData.value.studyStyle ||
      !surveyData.value.occupation
    ) {
      await uiStore.openAlert('모든 항목을 선택해주세요.', '알림');
      isSubmitting.value = false;
      return;
    }

    const payload = {
      email: signupInfo.email.trim(),
      nickname: signupInfo.tempNickname,
      isServiceAgreed: signupInfo.isServiceAgreed,
      isVideoAgreed: signupInfo.isVideoAgreed,
      isPersonaAgreed: signupInfo.isPersonaAgreed,
      studyType: surveyData.value.studyStyle,
      activeTime: surveyData.value.preferredTimeSlots,
      jobType: surveyData.value.occupation,
      averageHours: surveyData.value.avgStudyTime,
      targetHours: surveyData.value.goalStudyTime,
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
    await uiStore.openAlert(
      '회원가입 중 오류가 발생했습니다.\n다시 시도해주세요.',
      '오류'
    );
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