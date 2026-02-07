<template>
  <div class="fixed inset-0 bg-[rgba(255,253,245,0.65)] backdrop-blur-md flex items-center justify-center z-50" @click.self="$emit('close')">
    <div class="bg-[var(--color-cream2)] p-[2.5rem] rounded-[0.75rem] shadow-[4px_4px_0px_0px_var(--color-choco)] relative max-w-[26rem] w-full mx-[1rem] flex flex-col items-center gap-[1.5rem]">
      
      <button 
        @click="$emit('close')"
        class="absolute top-[1rem] right-[1rem] w-[2.625rem] h-[2.625rem] cursor-pointer hover:scale-110 transition-transform flex items-center justify-center"
      >
        <div class="w-[1.5rem] h-[1.5rem] relative">
          <div class="absolute top-[4px] left-[4px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[8px] left-[8px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[11px] left-[11px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[14px] left-[14px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[18px] left-[18px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[4px] left-[18px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[8px] left-[14px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[11px] left-[11px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[14px] left-[8px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[18px] left-[4px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
        </div>
      </button>

      <div class="flex flex-col items-center gap-[1.5rem] w-full">
        <div class="text-[var(--color-choco)] text-[2rem] font-['Xcu'] font-medium leading-none text-center w-full">
          비밀번호 입력
        </div>
        
        <div class="relative w-full">
          <input
            v-model="password"
            :type="isPasswordVisible ? 'text' : 'password'"
            class="border-2 border-[var(--color-choco)] rounded-[0.5rem] pl-4 pr-12 py-3 w-full text-[1.25rem] text-[var(--color-choco)] font-['PfStardust30S'] bg-[var(--color-cream)] focus:outline-none focus:border-[var(--color-choco)]"
            placeholder="비밀번호를 입력하세요"
            @keyup.enter="submit"
            autocomplete="off"
            autofocus
          />
          <!-- 입력값이 있을 때만 아이콘 노출 -->
          <button
            v-if="password"
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

        <div class="flex w-full mt-2 justify-center">
          <button @click="submit" class="butter-btn w-[6rem] h-[2.5rem] !rounded-[1.5rem] !px-[5px] py-[7px]">
            <span class="butter-btn-text text-[15px]">입장</span>
          </button>
        </div>
        <p v-if="localError" class="text-red-500 text-sm text-center w-full mt-2">{{ localError }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';

const password = ref('');
const isPasswordVisible = ref(false);
const isEyeHover = ref(false);

const emit = defineEmits(['submit', 'close']);
const props = defineProps<{ error?: string }>();
const localError = ref('');

watch(() => props.error, (val) => {
  localError.value = val || '';
});

function submit() {
  if (!password.value) {
    localError.value = '비밀번호를 입력하세요';
    return;
  }
  localError.value = '';
  emit('submit', password.value);
}
</script>