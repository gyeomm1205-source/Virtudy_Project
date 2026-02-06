import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useUiStore = defineStore('ui', () => {
  const isAlertOpen = ref(false);
  const alertTitle = ref('');
  const alertMessage = ref('');
  
  // Promise의 '해결(resolve)' 함수를 저장할 변수
  const resolvePromise = ref<((value: boolean) => void) | null>(null);

  // 이 함수는 Promise를 반환합니다 (await 가능)
  const openAlert = (message: string, title = '알림'): Promise<boolean> => {
    alertMessage.value = message;
    alertTitle.value = title;
    isAlertOpen.value = true;

    // 새로운 Promise를 만들어서 반환 (여기서 코드 실행이 대기 상태가 됨)
    return new Promise((resolve) => {
      // 나중에 호출하기 위해 resolve 함수를 변수에 저장해둠
      resolvePromise.value = resolve;
    });
  };

  const closeAlert = () => {
    isAlertOpen.value = false;
    // 혹시 닫힐 때까지 아무것도 안 눌렀다면 false 반환 (선택사항)
    if (resolvePromise.value) {
      resolvePromise.value(false);
      resolvePromise.value = null;
    }
  };

  const handleAlertConfirm = () => {
    // 저장해둔 resolve 함수를 실행하여 "기다림"을 끝냄
    if (resolvePromise.value) {
      resolvePromise.value(true); // true를 반환하며 await 종료
      resolvePromise.value = null;
    }
    isAlertOpen.value = false; // 모달 닫기
  };

  return {
    isAlertOpen,
    alertTitle,
    alertMessage,
    openAlert,
    closeAlert,
    handleAlertConfirm
  };
});