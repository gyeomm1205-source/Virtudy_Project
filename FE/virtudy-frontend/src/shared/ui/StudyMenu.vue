<template>
  <div class="study-menu-container h-[182px] w-[473px] flex">
    <!-- 랜덤 매칭 버튼 -->
    <button 
      @click="onRandomMatch"
      class="random-match-btn h-[182px] w-[192px] relative cursor-pointer hover:scale-105 transition-transform"
    >
      <div class="absolute bg-[var(--color-butter)] border-2 border-[var(--color-choco)] border-solid h-[182px] left-0 top-0 w-[192px]" style="box-shadow: 4px 4px 0px 0px var(--color-choco);" />
      <div class="absolute flex flex-col h-[85px] justify-center leading-normal left-1/2 top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[69px] ">
        <p class="text-[var(--color-choco)] text-[36px] font-['Ram'] font-medium text-center mb-0">랜덤</p>
        <p class="text-[var(--color-choco)] text-[36px] font-['Ram'] font-medium text-center">매칭</p>
      </div>
    </button>
    
    <!-- 오른쪽 메뉴 버튼들 -->
    <div class="flex flex-col w-[255px] ml-[26px]">
      <!-- 방만들기 버튼 -->
      <button 
        @click="onCreateRoom"
        class="create-room-btn h-[86px] w-[255px] relative cursor-pointer hover:scale-105 transition-transform"
      >
        <div class="absolute bg-[var(--color-cream)] border-2 border-[var(--color-choco)] border-solid h-[83px] left-0 top-1 w-[255px]" style="box-shadow: 4px 4px 0px 0px var(--color-choco);" />
        <div class="absolute flex flex-col h-[86px] justify-center leading-none left-1/2 top-1/2 transform -translate-x-1/2 -translate-y-1/2">
          <p class="text-[var(--color-choco)] text-[28px] font-['Xcu'] font-medium text-center w-[117px]">
            방만들기
          </p>
        </div>
      </button>
      
      <!-- 전체목록 버튼 -->
      <button 
        @click="onShowRoomList"
        class="room-list-btn h-[87px] w-[255px] relative mt-2 cursor-pointer hover:scale-105 transition-transform"
      >
        <div class="absolute bg-[var(--color-cream)] border-2 border-[var(--color-choco)] border-solid h-[86px] left-0 top-1 w-[255px]" style="box-shadow: 4px 4px 0px 0px var(--color-choco);" />
        <div class="absolute flex flex-col h-[86px] justify-center leading-none left-1/2 top-1/2 transform -translate-x-1/2 -translate-y-1/2">
          <p class="text-[var(--color-choco)] text-[28px] font-['Xcu'] font-medium text-center w-[122px]">
            전체목록
          </p>
        </div>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
// Emits 정의
const emit = defineEmits<{
  randomMatch: [];
  createRoom: [];
  showRoomList: [];
}>();

// 이벤트 핸들러
const onRandomMatch = () => {
  emit('randomMatch');
};

const onCreateRoom = () => {
  emit('createRoom');
};

const onShowRoomList = () => {
  emit('showRoomList');
};
</script>