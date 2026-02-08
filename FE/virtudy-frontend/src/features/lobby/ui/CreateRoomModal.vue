<script setup lang="ts">
import { ref, computed, reactive } from 'vue';
import { useLobby } from '../logic/useLobby';
import type { CreateRoomReq, RoomData } from '../types/lobby.types';

const props = defineProps<{
  initialData?: RoomData | null;
}>();

const emit = defineEmits(['close', 'success']);

const { createRoom, updateRoom, isLoading } = useLobby();

const isEditMode = computed(() => !!props.initialData);

const form = ref<CreateRoomReq>({
  title: props.initialData?.title || '',
  description: props.initialData?.description || '',
  type: props.initialData?.type || 'PUBLIC',
  password: '' 
});

// 비밀번호 표시/숨기기 상태
const isPasswordVisible = ref(false);
const isEyeHover = ref(false);

// 에러 상태 관리
const errors = reactive({
  title: '',
  password: ''
});

// 입력 시 에러 초기화 함수
const clearError = (field: 'title' | 'password') => {
  errors[field] = '';
};

const handleSubmit = async () => {
  // 0. 에러 초기화
  errors.title = '';
  errors.password = '';
  let hasError = false;

  // A. 유효성 검사 (Alert 대신 에러 상태 업데이트)
  if (!form.value.title.trim()) {
    errors.title = '방 제목을 입력해주세요!';
    hasError = true;
  } else if (form.value.title.length > 20) {
    errors.title = '방 제목은 20자 이내로 입력해주세요!';
    hasError = true;
  }

  if (!isEditMode.value && form.value.type === 'PRIVATE' && !form.value.password?.trim()) {
    errors.password = '비밀번호를 입력해주세요.';
    hasError = true;
  }

  // 에러가 하나라도 있으면 중단
  if (hasError) return;

  let success = false;

  // B. 분기 처리
  if (isEditMode.value && props.initialData?.roomId) {
    success = await updateRoom(props.initialData.roomId, form.value);
  } else {
    success = await createRoom(form.value);
  }
  
  // C. 성공 시 처리
  if (success) {
    emit('success'); 
    emit('close');   
  }
};
</script>

<template>
  <div class="fixed inset-0 bg-black/50 z-[999] flex items-center justify-center backdrop-blur-sm" @click.self="$emit('close')">
    
    <div class="bg-[var(--color-cream2)] w-[480px] rounded-[24px] border-2 border-[var(--color-choco)] p-8 shadow-[4px_4px_0px_0px_var(--color-choco)] transform transition-all relative">
      
      <button 
        @click="$emit('close')"
        class="absolute top-4 right-4 w-[2.625rem] h-[2.625rem] cursor-pointer hover:scale-110 transition-transform flex items-center justify-center"
        aria-label="닫기"
      >
        <div class="w-[1.5rem] h-[1.5rem] relative">
          <div class="absolute top-[4px] left-[4px] w-[2px] h-[2px] bg-[var(--color-choco)] rotate-45"></div>
          <div class="absolute top-[8px] left-[8px] w-[2px] h-[2px] bg-[var(--color-choco)] rotate-45"></div>
          <div class="absolute top-[11px] left-[11px] w-[2px] h-[2px] bg-[var(--color-choco)] rotate-45"></div>
          <div class="absolute top-[14px] left-[14px] w-[2px] h-[2px] bg-[var(--color-choco)] rotate-45"></div>
          <div class="absolute top-[18px] left-[18px] w-[2px] h-[2px] bg-[var(--color-choco)] rotate-45"></div>
          <div class="absolute top-[4px] left-[18px] w-[2px] h-[2px] bg-[var(--color-choco)] rotate-45"></div>
          <div class="absolute top-[8px] left-[14px] w-[2px] h-[2px] bg-[var(--color-choco)] rotate-45"></div>
          <div class="absolute top-[11px] left-[11px] w-[2px] h-[2px] bg-[var(--color-choco)] rotate-45"></div>
          <div class="absolute top-[14px] left-[8px] w-[2px] h-[2px] bg-[var(--color-choco)] rotate-45"></div>
          <div class="absolute top-[18px] left-[4px] w-[2px] h-[2px] bg-[var(--color-choco)] rotate-45"></div>
        </div>
      </button>

      <h2 class="text-[var(--color-choco)] text-[32px] font-['Ram'] text-center mb-6">
        {{ isEditMode ? '방 정보 수정' : '방 만들기' }}
      </h2>

      <div class="flex flex-col gap-5">
        <div class="flex flex-col gap-2">
          <label class="text-[var(--color-choco)] font-['PfStardust30S'] text-[1.125rem] pl-2">방 제목</label>
          <input 
            v-model="form.title" 
            type="text" 
            placeholder="예) 알고리즘 뿌시기" 
            class="input-box"
            :class="{ '!border-[var(--color-jam)] focus:!bg-[var(--color-jam)]/10': errors.title }" 
            maxlength="20"
            autofocus
            @input="clearError('title')"
          />
          <div class="flex justify-between items-center mt-[-0.25rem] px-1 h-[1rem]">
            <span v-if="errors.title" class="text-xs text-[var(--color-jam)] font-['PfStardust30S'] animate-pulse">{{ errors.title }}</span>
            <span v-else class="text-xs text-[var(--color-syrup)]">최대 20자까지 입력할 수 있어요.</span>
            
            <span class="text-xs text-[var(--color-choco)]">{{ form.title.length }}/20</span>
          </div>
        </div>

        <div class="flex flex-col gap-2">
          <label class="text-[var(--color-choco)] font-['PfStardust30S'] text-[1.125rem] pl-2">설명</label>
          <textarea 
            v-model="form.description" 
            placeholder="어떤 스터디인지 알려주세요!" 
            class="input-box h-24 resize-none"
          ></textarea>
        </div>
        <!-- 설명 박스 아래에 비공개방 체크박스 추가 -->
        <div v-if="!isEditMode || (isEditMode && form.type !== 'PRIVATE' && form.type !== 'PUBLIC')" class="flex items-center gap-[0.1rem] mt-[-0.5rem]">
          <div class="w-[2rem] flex items-center justify-center">
            <input
              type="checkbox"
              id="privateRoomCheckbox"
              :checked="form.type === 'PRIVATE'"
              :disabled="isEditMode"
              @change="form.type = ($event.target as HTMLInputElement).checked ? 'PRIVATE' : 'PUBLIC'"
              class="w-[1.125rem] h-[1.125rem] accent-[var(--color-choco)] cursor-pointer"
            />
          </div>
          <label for="privateRoomCheckbox" class="text-[var(--color-choco)] font-['PfStardust30S'] text-[1rem] select-none cursor-pointer">
            비공개방으로 설정
          </label>
        </div>

        <transition name="slide-fade">
          <div v-if="form.type === 'PRIVATE'" class="flex flex-col gap-2">
            <label class="text-[var(--color-choco)] font-['PfStardust30S'] text-[1.125rem] pl-2">
              비밀번호 {{ isEditMode ? '(변경 시 입력)' : '' }}
            </label>
            <div class="relative w-full">
              <input
                v-model="form.password"
                :type="isPasswordVisible ? 'text' : 'password'"
                placeholder="비밀번호 4자리 이상"
                class="input-box pr-12"
                :class="{ '!border-[var(--color-jam)] focus:!bg-[var(--color-jam)]/10': errors.password }"
                @input="clearError('password')"
                autocomplete="off"
              />
              <button
                v-if="form.password"
                type="button"
                class="absolute right-4 top-1/2 -translate-y-1/2 p-0 m-0 bg-transparent border-none cursor-pointer flex items-center justify-center"
                @click="isPasswordVisible = !isPasswordVisible"
                @mouseenter="isEyeHover = true"
                @mouseleave="isEyeHover = false"
                tabindex="-1"
                aria-label="비밀번호 보기/숨기기"
              >
                <span v-if="!isEyeHover">
                  <!-- 기본 SVG -->
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none">
                    <path d="M2 13H1V11H2V9H3V8H4V7H5V6H7V5H15V6H14V7H13V6H11V7H9V8H8V9H7V11H6V13H7V14H6V15H5V16H3V15H2V13Z" fill="#805143"/>
                    <path d="M9 11H8V12H9V11Z" fill="#805143"/>
                    <path d="M12 8H11V9H12V8Z" fill="#805143"/>
                    <path d="M9 17H8V18H7V19H6V20H5V21H4V22H3V21H2V20H3V19H4V18H5V17H6V16H7V15H8V14H9V13H10V12H11V11H12V10H13V9H14V8H15V7H16V6H17V5H18V4H19V3H20V2H21V3H22V4H21V5H20V6H19V7H18V8H17V9H16V10H15V11H14V12H13V13H12V14H11V15H10V16H9V17Z" fill="#805143"/>
                    <path d="M13 15H12V16H13V15Z" fill="#805143"/>
                    <path d="M14 14H13V15H14V14Z" fill="#805143"/>
                    <path d="M16 12H15V13H16V12Z" fill="#805143"/>
                    <path d="M15 13H14V14H15V13Z" fill="#805143"/>
                    <path d="M23 11V13H22V15H21V16H20V17H19V18H17V19H9V18H10V17H11V18H13V17H15V16H16V15H17V13H18V11H17V10H18V9H19V8H21V9H22V11H23Z" fill="#805143"/>
                  </svg>
                </span>
                <span v-else>
                  <!-- hover SVG -->
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none">
                    <path d="M16 11V13H15V14H14V15H13V16H11V15H10V14H9V13H8V11H10V10H11V8H13V9H14V10H15V11H16Z" fill="#805143"/>
                    <path d="M22 11V9H21V8H20V7H19V6H17V5H7V6H5V7H4V8H3V9H2V11H1V13H2V15H3V16H4V17H5V18H7V19H17V18H19V17H20V16H21V15H22V13H23V11H22ZM18 13H17V15H16V16H15V17H13V18H11V17H9V16H8V15H7V13H6V11H7V9H8V8H9V7H11V6H13V7H15V8H16V9H17V11H18V13Z" fill="#805143"/>
                  </svg>
                </span>
              </button>
            </div>
            <div class="h-[1rem] mt-[-0.25rem] px-1">
              <span v-if="errors.password" class="text-xs text-[var(--color-jam)] font-['PfStardust30S'] animate-pulse">{{ errors.password }}</span>
            </div>
          </div>
        </transition>

      </div>

      <div class="mt-8 flex justify-center">
        <button
          class="butter-btn w-[7rem] h-[2.5rem] !rounded-[1.5rem] !px-[5px] py-[7px] disabled:opacity-50 disabled:cursor-not-allowed"
          @click="handleSubmit"
          :disabled="isLoading"
        >
          <span class="butter-btn-text text-[15px]">
            {{ isLoading ? '처리 중...' : (isEditMode ? '수정 완료!' : '만들기 완료!') }}
          </span>
        </button>
      </div>

    </div>
  </div>
</template>

<style scoped>
/* 입력창 공통 스타일 */
.input-box {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid var(--color-choco);
  border-radius: 12px;
  font-size: 18px;
  color: var(--color-choco);
  font-family: 'PfStardust30S', sans-serif;
  transition: all 0.2s;
  background-color: var(--color-cream);
}

/* 포커스 시 초코색 테두리 */
.input-box:focus {
  border-color: var(--color-choco);
  outline: none;
  background-color: var(--color-cream2);
}

/* 플레이스홀더 색상 */
.input-box::placeholder {
  color: var(--color-syrup);
  opacity: 0.7;
}

/* 애니메이션: 슬라이드 효과 */
.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}
.slide-fade-leave-active {
  transition: all 0.2s cubic-bezier(1, 0.5, 0.8, 1);
}
.slide-fade-enter-from,
.slide-fade-leave-to {
  transform: translateY(-10px);
  opacity: 0;
}
.butter-btn-text {
  font-size: 21px;
}
</style>