<template>
  <div>
    <h1>서비스 이용 약관 동의</h1>

    <div>
      <textarea readonly rows="6" cols="70">
[ Virtudy 서비스 이용 약관 ]

제 1 조 (목적)
본 약관은 Virtudy가 제공하는 모든 서비스의 이용 조건 및 절차, 회원과 회사 간의 권리, 의무, 책임사항 등 기타 필요한 사항을 규정함을 목적으로 합니다.

제 2 조 (용어의 정의)
본 약관에서 사용하는 용어의 정의는 다음과 같습니다.
1. "서비스"라 함은 회사가 제공하는 모든 제반 서비스를 의미합니다.
2. "회원"이라 함은 회사와 서비스 이용계약을 체결하고 이용자 아이디(ID)를 부여받은 자를 의미합니다.

(이하 생략)
      </textarea>
      <br />
      <label>
        <input type="checkbox" v-model="isServiceAgreed" />
        위의 서비스 이용 약관에 동의합니다. (필수)
      </label>
    </div>

    <div>
      <h2>영상정보 수집 및 이용 동의</h2>
      <textarea readonly rows="6" cols="70">
[ 영상정보 수집 및 이용 동의 ]

제 1 조 (수집 목적)
회사는 회원의 학습 집중도 분석 등 서비스 제공을 목적으로 웹캠을 통해 영상 정보를 수집할 수 있습니다.

(이하 생략)
      </textarea>
      <br />
      <label>
        <input type="checkbox" v-model="isVideoAgreed" />
        위의 영상정보 수집 및 이용에 동의합니다. (필수)
      </label>
    </div>

    <div>
      <h2>개인정보 제3자 제공 동의</h2>
      <textarea readonly rows="6" cols="70">
[ 개인정보 제3자 제공 동의 ]

제 1 조 (제공 목적)
회사는 더 나은 서비스 제공 및 통계 분석을 위해 개인정보를 제3자에게 제공할 수 있습니다.

(이하 생략)
      </textarea>
      <br />
      <label>
        <input type="checkbox" v-model="isPersonaAgreed" />
        위의 개인정보 제3자 제공에 동의합니다. (필수)
      </label>
    </div>

    <br />
    <button @click="goToNext" :disabled="!allAgreed">다음</button>
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

const allAgreed = computed(
  () => isServiceAgreed.value && isVideoAgreed.value && isPersonaAgreed.value
);

const goToNext = () => {
  if (allAgreed.value) {
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
div {
  padding: 20px;
}
textarea {
  resize: none;
  margin-top: 5px;
  margin-bottom: 5px;
}
div > div {
  margin-bottom: 15px;
}
</style>