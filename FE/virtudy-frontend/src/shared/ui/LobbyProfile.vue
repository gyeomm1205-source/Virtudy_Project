<template>
  <div class="bg-[var(--color-syrup)] border-2 border-[var(--color-choco)] border-solid h-[406px] w-[255px] relative overflow-clip rounded-[20px]" style="box-shadow: 4px 4px 0px 0px var(--color-choco);">
    <!-- 프로필 이미지 -->
    <div class="absolute left-1/2 top-[84px] transform -translate-x-1/2">
      <div class="relative w-[146px] h-[146px]">
        <!-- 배경 원형 프레임 -->
        <img 
          :src="profileFrameUrl" 
          alt="프로필 프레임"
          class="w-full h-full object-cover"
        />
        <!-- 실제 프로필 이미지 -->
        <div class="absolute top-[6px] left-[8px] w-[146px] h-[146px] rounded-full overflow-hidden">
          <img 
            :src="userProfileImage || defaultProfileImage" 
            alt="프로필 이미지"
            class="w-full h-full object-cover transform rotate-180 scale-y-[-1]"
          />
        </div>
      </div>
    </div>
    
    <!-- 메뉴 버튼들 -->
    <div class="absolute bottom-0 left-0 w-full flex flex-col gap-[10px] items-center justify-center p-[10px]">
      <!-- 랜덤매칭 버튼 -->
      <button 
        @click="onRandomMatch"
        class="bg-[var(--color-butter)] border-2 border-[var(--color-choco)] border-solid w-full px-[32px] py-[20px] rounded-[2px]"
        style="box-shadow: 4px 4px 0px 0px var(--color-choco);"
      >
        <span class="text-[var(--color-choco)] text-[28px] font-['Xcu'] font-medium leading-none">
          랜덤매칭
        </span>
      </button>
      
      <!-- 방만들기 버튼 -->
      <button 
        @click="onCreateRoom"
        class="bg-[var(--color-butter2)] border-2 border-[var(--color-choco)] border-solid w-full px-[32px] py-[20px] rounded-[2px]"
        style="box-shadow: 4px 4px 0px 0px var(--color-choco);"
      >
        <span class="text-[var(--color-choco)] text-[28px] font-['Xcu'] font-medium leading-none">
          방만들기
        </span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
interface LobbyProfileProps {
  userProfileImage?: string;
}

// Props 정의
const props = withDefaults(defineProps<LobbyProfileProps>(), {});

// Emits 정의
const emit = defineEmits<{
  randomMatch: [];
  createRoom: [];
}>();

// 이벤트 핸들러
const onRandomMatch = () => {
  emit('randomMatch');
};

const onCreateRoom = () => {
  emit('createRoom');
};

// 이미지 URLs
const profileFrameUrl = "/vite.svg"; // [수정] 임시 플레이스홀더
const defaultProfileImage = "/vite.svg"; // [수정] 임시 플레이스홀더
</script>