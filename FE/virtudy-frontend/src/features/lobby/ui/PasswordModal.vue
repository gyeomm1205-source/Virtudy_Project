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
            autofocus
          />
          
          <div 
            class="absolute right-3 top-1/2 transform -translate-y-1/2 cursor-pointer flex items-center justify-center transition-opacity opacity-40 hover:opacity-100 active:opacity-100"
            @click="isPasswordVisible = !isPasswordVisible"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M16 11V13H15V14H14V15H13V16H11V15H10V14H9V13H8V11H10V10H11V8H13V9H14V10H15V11H16Z" fill="#805143"/>
              <path d="M22 11V9H21V8H20V7H19V6H17V5H7V6H5V7H4V8H3V9H2V11H1V13H2V15H3V16H4V17H5V18H7V19H17V18H19V17H20V16H21V15H22V13H23V11H22ZM18 13H17V15H16V16H15V17H13V18H11V17H9V16H8V15H7V13H6V11H7V9H8V8H9V7H11V6H13V7H15V8H16V9H17V11H18V13Z" fill="#805143"/>
            </svg>
          </div>
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