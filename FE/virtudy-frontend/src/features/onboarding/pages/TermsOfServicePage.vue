<template>
  <div class="min-h-screen bg-[var(--color-cream2)]">
    
    <div class="text-center pt-[6rem] pb-[2rem]">
      <h1 class="text-[var(--color-syrup)] text-[2.625rem] font-['Ram'] leading-[3rem] font-medium tracking-[-0.0525rem]">
        약관동의
      </h1>
    </div>

    <div class="flex justify-center items-center mb-[2rem]">
      <label class="flex items-center gap-[0.75rem] cursor-pointer select-none">
        <div class="w-[2rem] flex items-center justify-center">
          <input
            id="agree-all"
            type="checkbox"
            v-model="allAgreed"
            class="w-[1.5rem] h-[1.5rem] accent-[var(--color-choco)] cursor-pointer"
          >
        </div>
        <span class="text-[var(--color-choco)] text-[1.75rem] font-['PfStardust30S'] font-normal">
          약관에 모두 동의합니다.
        </span>
      </label>
    </div>

    <div class="flex flex-col items-center pb-[4rem]">
      <div class="max-w-[50rem] w-full px-[2rem]">
        
        <div class="text-[var(--color-choco)] font-['PfStardust30S'] leading-normal mb-[2rem]">
          <p class="text-[1.75rem] mb-[1rem] font-normal">[ Virtudy 서비스 이용 약관 ]</p>
          
          <div class="terms-scroll text-[1.125rem] text-[var(--color-syrup)] h-[12rem] overflow-y-auto border border-[var(--color-choco)] rounded p-[1.5rem] mb-[1rem] bg-white">
            <div class="space-y-[1rem]">
              <div>
                <strong class="block mb-2">제 1 조 (목적)</strong>
                <p class="font-normal text-[0.95em] leading-relaxed">
                  본 약관은 주식회사 Virtudy(이하 "회사")가 제공하는 가상 스터디 플랫폼 서비스 및 관련 제반 서비스(이하 "서비스")의 이용과 관련하여 회사와 회원 간의 권리, 의무 및 책임사항, 기타 필요한 사항을 규정함을 목적으로 합니다.
                </p>
              </div>
              <div>
                <strong class="block mb-2">제 2 조 (용어의 정의)</strong>
                <p class="font-normal text-[0.95em] leading-relaxed">
                  1. "서비스"라 함은 구현되는 단말기(PC, 휴대형 단말기 등 각종 유무선 장치를 포함)와 상관없이 회원이 이용할 수 있는 Virtudy 및 관련 제반 서비스를 의미합니다.<br>
                  2. "회원"이라 함은 회사의 서비스에 접속하여 본 약관에 따라 회사와 이용계약을 체결하고 회사가 제공하는 서비스를 이용하는 고객을 말합니다.<br>
                  3. "아바타"라 함은 서비스 내에서 회원을 대리하는 가상의 캐릭터를 의미합니다.
                </p>
              </div>
              <div>
                <strong class="block mb-2">제 3 조 (약관의 효력 및 변경)</strong>
                <p class="font-normal text-[0.95em] leading-relaxed">
                  1. 회사는 본 약관의 내용을 회원이 쉽게 알 수 있도록 서비스 초기 화면에 게시합니다.<br>
                  2. 회사는 관련 법령을 위배하지 않는 범위에서 본 약관을 개정할 수 있습니다.
                </p>
              </div>
              <div>
                <strong class="block mb-2">제 4 조 (회원의 의무)</strong>
                <p class="font-normal text-[0.95em] leading-relaxed">
                  회원은 다음 행위를 하여서는 안 됩니다.<br>
                  1. 신청 또는 변경 시 허위 내용의 등록<br>
                  2. 타인의 정보 도용<br>
                  3. 회사가 게시한 정보의 변경<br>
                  4. 서비스 내에서 욕설, 비방 등 공서양속에 반하는 행위
                </p>
              </div>
            </div>
          </div>
          
          <label class="flex items-center gap-[0.75rem] cursor-pointer select-none">
            <div class="w-[2rem] flex items-center justify-center">
              <input
                id="agree-service"
                type="checkbox"
                v-model="isServiceAgreed"
                class="w-[1.125rem] h-[1.125rem] accent-[var(--color-choco)] cursor-pointer"
              >
            </div>
            <span class="text-[var(--color-choco)] text-[1.125rem] font-['PfStardust30S'] font-normal">
              위의 서비스 이용 약관에 동의합니다. (필수)
            </span>
          </label>
        </div>

        <div class="text-[var(--color-choco)] font-['PfStardust30S'] leading-normal mb-[2rem]">
          <p class="text-[1.75rem] mb-[1rem] font-normal">[ 영상정보 수집 및 이용 동의 ]</p>
          
          <div class="terms-scroll text-[1.125rem] text-[var(--color-syrup)] h-[12rem] overflow-y-auto border border-[var(--color-choco)] rounded p-[1.5rem] mb-[1rem] bg-white">
            <div class="space-y-[1rem]">
              <div>
                <strong class="block mb-2">제 1 조 (수집 및 이용 목적)</strong>
                <p class="font-normal text-[0.95em] leading-relaxed">
                  회사는 다음과 같은 목적을 위해 회원의 웹캠 영상 정보를 수집 및 이용합니다.<br>
                  1. AI 기반 학습 집중도 분석 및 통계 제공<br>
                  2. 사용자의 표정 및 움직임을 반영한 아바타 상태 동기화<br>
                  3. 부정 행위 방지 및 스터디 참여 확인
                </p>
              </div>
              <div>
                <strong class="block mb-2">제 2 조 (수집하는 영상정보의 항목)</strong>
                <p class="font-normal text-[0.95em] leading-relaxed">
                  - 수집 항목: PC 또는 모바일 기기의 카메라를 통해 입력되는 실시간 영상 데이터<br>
                  - 분석 정보: 눈 깜빡임, 시선 처리, 고개 움직임 등을 통한 집중도 수치 데이터
                </p>
              </div>
              <div>
                <strong class="block mb-2">제 3 조 (보유 및 이용 기간)</strong>
                <p class="font-normal text-[0.95em] leading-relaxed">
                  <span class="text-red-500 font-bold">수집된 원본 영상 데이터는 실시간 분석 후 즉시 파기되며, 별도의 서버에 저장되지 않습니다.</span> 단, 분석된 결과 데이터(집중도 점수 등)는 회원 탈퇴 시까지 보관됩니다.
                </p>
              </div>
              <div>
                <strong class="block mb-2">제 4 조 (동의 거부 권리)</strong>
                <p class="font-normal text-[0.95em] leading-relaxed">
                  귀하는 영상정보 수집 및 이용에 대한 동의를 거부할 권리가 있습니다. 단, 동의를 거부할 경우 AI 집중도 분석 등 서비스의 주요 기능 이용이 제한될 수 있습니다.
                </p>
              </div>
            </div>
          </div>
          
          <label class="flex items-center gap-[0.75rem] cursor-pointer select-none">
            <div class="w-[2rem] flex items-center justify-center">
              <input
                id="agree-video"
                type="checkbox"
                v-model="isVideoAgreed"
                class="w-[1.125rem] h-[1.125rem] accent-[var(--color-choco)] cursor-pointer"
              >
            </div>
            <span class="text-[var(--color-choco)] text-[1.125rem] font-['PfStardust30S'] font-normal">
              위의 영상정보 수집 및 이용에 동의합니다. (필수)
            </span>
          </label>
        </div>

        <div class="text-[var(--color-choco)] font-['PfStardust30S'] leading-normal mb-[3rem]">
          <p class="text-[1.75rem] mb-[1rem] font-normal">[ 개인정보 제3자 제공 동의 ]</p>
          
          <div class="terms-scroll text-[1.125rem] text-[var(--color-syrup)] h-[12rem] overflow-y-auto border border-[var(--color-choco)] rounded p-[1.5rem] mb-[1rem] bg-white">
            <div class="space-y-[1rem]">
              <div>
                <strong class="block mb-2">제 1 조 (제공받는 자)</strong>
                <p class="font-normal text-[0.95em] leading-relaxed">
                  회사는 서비스 품질 향상 및 AI 모델 고도화를 위해 다음과 같이 개인정보를 제공할 수 있습니다.<br>
                  - 제공받는 자: Virtudy AI 연구소 및 제휴 클라우드 사업자 (AWS, Google Cloud 등)
                </p>
              </div>
              <div>
                <strong class="block mb-2">제 2 조 (제공 목적)</strong>
                <p class="font-normal text-[0.95em] leading-relaxed">
                  1. 서비스 안정성 확보를 위한 서버 인프라 운영<br>
                  2. 학습 패턴 분석 알고리즘의 정확도 개선<br>
                  3. 비식별화된 통계 데이터 생성
                </p>
              </div>
              <div>
                <strong class="block mb-2">제 3 조 (제공하는 항목)</strong>
                <p class="font-normal text-[0.95em] leading-relaxed">
                  - 서비스 이용 기록, 접속 로그, 학습 시간 데이터, 집중도 분석 결과값 (비식별 처리됨)
                </p>
              </div>
              <div>
                <strong class="block mb-2">제 4 조 (보유 및 이용 기간)</strong>
                <p class="font-normal text-[0.95em] leading-relaxed">
                  제공받는 자의 이용 목적 달성 시 또는 회원의 제3자 제공 동의 철회 시까지 보유 및 이용합니다.
                </p>
              </div>
            </div>
          </div>
          
          <label class="flex items-center gap-[0.75rem] cursor-pointer select-none">
            <div class="w-[2rem] flex items-center justify-center">
              <input
                id="agree-persona"
                type="checkbox"
                v-model="isPersonaAgreed"
                class="w-[1.125rem] h-[1.125rem] accent-[var(--color-choco)] cursor-pointer"
              >
            </div>
            <span class="text-[var(--color-choco)] text-[1.125rem] font-['PfStardust30S'] font-normal">
              위의 개인정보 제3자 제공에 동의합니다. (필수)
            </span>
          </label>
        </div>

        <div class="flex justify-center pt-[2rem]">
          <button 
            @click="goToNext" 
            :disabled="!canProceed"
            class="bg-[var(--color-butter)] border-2 border-[var(--color-choco)] px-[2rem] py-[1.25rem] rounded-[0.125rem] font-['Xcu'] text-[1.75rem] text-[var(--color-choco)] font-medium leading-none transition-all duration-200"
            :class="{
              'opacity-50 cursor-not-allowed': !canProceed,
              'hover:scale-105 active:scale-95 cursor-pointer': canProceed
            }"
            style="box-shadow: 4px 4px 0px 0px var(--color-choco);">
            시작하기
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';

const router = useRouter();
const authStore = useAuthStore();

const isServiceAgreed = ref(false);
const isVideoAgreed = ref(false);
const isPersonaAgreed = ref(false);

const allAgreed = computed({
  get() {
    return (
      isServiceAgreed.value &&
      isVideoAgreed.value &&
      isPersonaAgreed.value
    );
  },
  set(value: boolean) {
    isServiceAgreed.value = value;
    isVideoAgreed.value = value;
    isPersonaAgreed.value = value;
  },
});

const canProceed = computed(() => allAgreed.value);

const goToNext = () => {
  if (canProceed.value) {
    const currentInfo = authStore.signupInfo || {};
    authStore.setSignupInfo({
      ...currentInfo,
      isServiceAgreed: isServiceAgreed.value,
      isVideoAgreed: isVideoAgreed.value,
      isPersonaAgreed: isPersonaAgreed.value,
    });
    // 약관 동의 후 성향 조사 페이지로 이동
    router.push({ name: 'survey' });
  }
};
</script>

<style scoped>
.terms-scroll {
  scrollbar-color: var(--color-choco) var(--color-cream2);
  scrollbar-width: thin;
}

.terms-scroll::-webkit-scrollbar {
  width: 8px;
}

.terms-scroll::-webkit-scrollbar-track {
  background: var(--color-cream2);
}

.terms-scroll::-webkit-scrollbar-thumb {
  background-color: var(--color-choco);
  border-radius: 999px;
  border: 2px solid var(--color-cream2);
}
</style>

<style scoped>
/* 스크롤바 숨기기 유틸리티 (필요시 추가) */
.scrollbar-hide::-webkit-scrollbar {
    display: none;
}
.scrollbar-hide {
    -ms-overflow-style: none;
    scrollbar-width: none;
}
</style>