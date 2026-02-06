<template>
  <GlobalBackground :skyType="3">
  <div class="relative min-h-[85rem] w-full pb-[8rem] mypage-root">
    
    <div 
      class="fixed inset-0 bg-black/20 backdrop-blur-sm flex items-center justify-center z-50" 
      @click.self="$emit('close')"
    >
      <div class="bg-[var(--color-cream2)] p-[2.5rem] rounded-[0.75rem] shadow-[4px_4px_0px_0px_var(--color-choco)] relative max-w-[35rem] w-full mx-[1rem]">
        
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

        <div class="flex flex-col gap-[2.5rem] pb-[0rem] ">
          <div class="flex flex-col gap-[1.625rem]">
            <div class="flex flex-col gap-[1.25rem] items-center justify-center">
              <div class="relative">
                <div class="w-[9rem] h-[9rem] rounded-full bg-[var(--color-butter)] flex items-center justify-center overflow-hidden">
                  <div
                    class="w-[10.5rem] h-[11.5rem]"
                    :style="{ transform: `translate(${avatarOffsetX}, ${avatarOffsetY})` }"
                  >
                    <CharacterAvatar
                      v-if="hasAvatarConfig"
                      :config="avatar!"
                      class="absolute inset-0 w-full h-full object-cover scale-[1.16] origin-center translate-y-[17.5%] translate-x-[0.35rem]"
                    />
                    <img
                      v-else-if="avatarImageUrl"
                      :src="avatarImageUrl"
                      alt="프로필 사진"
                      class="w-full h-full object-cover"
                    />
                  </div>
                </div>
                <button
                  type="button"
                  @click="goToAvatar"
                  class="absolute inset-0 flex flex-col items-center justify-center text-[1.35rem] font-['Xcu'] text-[var(--color-cream2)] rounded-full bg-black/30 leading-tight"
                >
                  <span>아바타</span>
                  <span>수정하기</span>
                </button>
              </div>
              
              <div class="text-[var(--color-syrup)] text-[1.75rem] font-['Xcu'] font-medium leading-none">
                {{ nickName }}
              </div>
              
              <div class="text-[var(--color-syrup)] text-[1.25rem] font-['PfStardust30S'] font-normal leading-none tracking-[-0.05rem]">
                {{ email || '이메일@카카오.com' }}
              </div>
              
              <div class="w-[29.4375rem] h-[1px] bg-[var(--color-syrup)] opacity-30"></div>
            </div>

            <div class="flex flex-col gap-[1.5rem]">
              <div class="flex items-center justify-between w-[29.1875rem] font-['PfStardust30S'] text-[1.5rem] leading-none">
                <span class="text-[var(--color-choco)]">닉네임</span>
                <input 
                  :value="nickName" 
                  @input="$emit('update:nickName', ($event.target as HTMLInputElement).value)"
                  placeholder="기존 닉네임(여기서 변경)"
                  class="text-[var(--color-syrup)] bg-transparent border-none outline-none text-right placeholder-[var(--color-syrup)]"
                />
              </div>
              
              <div class="w-[29.4375rem] h-[1px] bg-[var(--color-syrup)] opacity-30"></div>
              
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
              
              <div class="w-[29.4375rem] h-[1px] bg-[var(--color-syrup)] opacity-30"></div>
            </div>
          </div>

          <div class="flex justify-start mt-[0.5rem]">
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
  </div>
  </GlobalBackground>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { JOB_OPTIONS } from '../types/mypage.types';
import CharacterAvatar from '@/shared/ui/avatar/CharacterAvatar.vue';
import type { AvatarConfig } from '@/shared/types/common.types';
import { useUiStore } from '@/stores/uiStore'; 

const router = useRouter();
const uiStore = useUiStore(); 

const props = withDefaults(defineProps<{
  email: string;
  nickName: string;
  jobType: string;
  jobOptions: typeof JOB_OPTIONS;
  avatar?: AvatarConfig;
  avatarImageUrl?: string;
  avatarOffsetX?: string;
  avatarOffsetY?: string;
}>(), {
  avatarOffsetX: '0.66rem',
  avatarOffsetY: '2.6rem',
});

defineEmits(['close', 'submit', 'update:nickName', 'update:jobType']);

const hasAvatarConfig = computed(() => {
  if (!props.avatar) return false;
  return Object.values(props.avatar).some((value) => Boolean(value));
});

// [이후 수정] 만약 AvatarCreationPage와 카운트 로직을 공유한다면
// KEY나 로직을 맞춰주는 것이 좋음
const AVATAR_CREATE_LIMIT = 3;
const AVATAR_CREATE_KEY = 'avatarCreateCount';

const goToAvatar = async () => {
  const count = parseInt(localStorage.getItem(AVATAR_CREATE_KEY) || '0', 10);
  const remain = AVATAR_CREATE_LIMIT - (isNaN(count) ? 0 : count);
  if (remain <= 0) {
    await uiStore.openAlert('아바타 생성 기회를 모두 사용하셨습니다.', '알림');
    return;
  }
  router.push('/avatar/create');
};
</script>