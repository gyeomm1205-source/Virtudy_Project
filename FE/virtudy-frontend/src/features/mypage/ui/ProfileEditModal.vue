<template>
  <!-- 모달 오버레이 -->
  <div 
  class="fixed inset-0 bg-black/20 backdrop-blur-sm flex items-center justify-center z-50" 
  @click.self="$emit('close')"
  >
    <!-- 모달 콘텐츠 -->
    <div class="bg-[var(--color-cream2)] p-[2.5rem] rounded-[0.75rem] shadow-[4px_4px_0px_0px_var(--color-choco)] relative max-w-[35rem] w-full mx-[1rem]">
      
      <!-- 닫기 버튼 -->
      <button 
        @click="$emit('close')"
        class="absolute top-[1rem] right-[1rem] w-[2.625rem] h-[2.625rem] cursor-pointer hover:scale-110 transition-transform flex items-center justify-center"
      >
        <div class="w-[1.5rem] h-[1.5rem] relative">
          <!-- 마름모 점선 X 패턴 - 중심 기준 대칭 -->
          <!-- 왼쪽 위에서 오른쪽 아래 대각선 -->
          <div class="absolute top-[4px] left-[4px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[8px] left-[8px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[11px] left-[11px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[14px] left-[14px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[18px] left-[18px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          
          <!-- 오른쪽 위에서 왼쪽 아래 대각선 -->
          <div class="absolute top-[4px] left-[18px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[8px] left-[14px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[11px] left-[11px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[14px] left-[8px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
          <div class="absolute top-[18px] left-[4px] w-[2px] h-[2px] bg-[var(--color-choco)] transform rotate-45"></div>
        </div>
      </button>

      <div class="flex flex-col gap-[3rem] h-[31.125rem]">
        <!-- 상단 섹션: 프로필 이미지와 기본 정보 -->
        <div class="flex flex-col gap-[1.625rem]">
          <div class="flex flex-col gap-[1.25rem] items-center justify-center">
            <!-- 프로필 이미지 -->
            <div class="relative">
              <div class="w-[9.125rem] h-[9.125rem] rounded-full overflow-hidden border-4 border-[var(--color-choco)]">
                <div class="w-full h-full bg-[var(--color-butter)] flex items-center justify-center text-[1.75rem] font-['Xcu'] font-medium text-[var(--color-cream2)] text-center">
                  아바타<br/>수정
                </div>
              </div>
            </div>
            
            <!-- 닉네임 -->
            <div class="text-[var(--color-syrup)] text-[1.75rem] font-['Xcu'] font-medium leading-none">
              닉네임
            </div>
            
            <!-- 이메일 -->
            <div class="text-[var(--color-syrup)] text-[1.25rem] font-['PfStardust30S'] font-normal leading-none tracking-[-0.05rem]">
              {{ email || '이메일@카카오.com' }}
            </div>
            
            <!-- 구분선 -->
            <div class="w-[29.4375rem] h-[1px] bg-[var(--color-syrup)] opacity-30"></div>
          </div>

          <!-- 폼 필드들 -->
          <div class="flex flex-col gap-[1.5rem]">
            <!-- 닉네임 필드 -->
            <div class="flex items-center justify-between w-[29.1875rem] font-['PfStardust30S'] text-[1.5rem] leading-none">
              <span class="text-[var(--color-choco)]">닉네임</span>
              <input 
                :value="nickName" 
                @input="$emit('update:nickName', ($event.target as HTMLInputElement).value)"
                placeholder="기존 닉네임(여기서 변경)"
                class="text-[var(--color-syrup)] bg-transparent border-none outline-none text-right placeholder-[var(--color-syrup)]"
              />
            </div>
            
            <!-- 구분선 -->
            <div class="w-[29.4375rem] h-[1px] bg-[var(--color-syrup)] opacity-30"></div>
            
            <!-- 직업 필드 -->
            <div class="flex items-center justify-between w-[29.4375rem] font-['PfStardust30S'] text-[1.5rem] leading-none">
              <span class="text-[var(--color-choco)]">직업</span>
              <select 
                :value="jobType" 
                @change="$emit('update:jobType', ($event.target as HTMLSelectElement).value)"
                class="text-[var(--color-syrup)] bg-transparent border-none outline-none text-right appearance-none cursor-pointer"
              >
                <option v-for="opt in jobOptions" :key="opt.value" :value="opt.value" class="bg-[var(--color-cream2)] text-[var(--color-choco)]">
                  {{ opt.label }}
                </option>
              </select>
            </div>
            
            <!-- 구분선 -->
            <div class="w-[29.4375rem] h-[1px] bg-[var(--color-syrup)] opacity-30"></div>
          </div>
        </div>

        <!-- 하단: 변경하기 버튼 -->
        <div class="flex justify-start">
          <button 
            @click="$emit('submit')"
            class="butter-btn h-[2.25rem] px-[1.5rem] py-[0.75rem] !rounded-[0.8rem]"
          >
            <span class="butter-btn-text text-[1rem]">변경하기</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { JOB_OPTIONS } from '../types/mypage.types';

defineProps<{
  email: string;
  nickName: string;
  jobType: string;
  jobOptions: typeof JOB_OPTIONS;
}>();

defineEmits(['close', 'submit', 'update:nickName', 'update:jobType']);
</script>