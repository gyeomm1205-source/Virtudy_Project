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