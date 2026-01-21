<template>
  <div>
    <h1>서비스 이용 약관 동의</h1>
    <textarea readonly rows="10" cols="50">
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
      <input type="checkbox" v-model="agreed" />
      위의 모든 약관에 동의합니다.
    </label>
    <br />
    <button @click="goToNext" :disabled="!agreed">다음</button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const agreed = ref(false);
const router = useRouter();

const goToNext = () => {
  if (agreed.value) {
    // 약관 동의 후 성향 조사 페이지로 이동
    router.push('/survey');
  }
};
</script>

<style scoped>
div {
  padding: 20px;
}
textarea {
  resize: none;
}
</style>
