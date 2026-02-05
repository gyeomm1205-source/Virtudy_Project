<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useLobby } from '../logic/useLobby';
import type { CreateRoomReq, RoomData } from '../types/lobby.types';

// Props 정의 (수정 시 데이터를 받기 위해 initialData 추가)
const props = defineProps<{
  initialData?: RoomData | null; // null이면 생성 모드, 값이 있으면 수정 모드
}>();

// 부모에게 보낼 이벤트
const emit = defineEmits(['close', 'success']);

// 로직 연결 (updateRoom 추가 가져오기)
const { createRoom, updateRoom, isLoading } = useLobby();

// 수정 모드인지 판별하는 Computed
const isEditMode = computed(() => !!props.initialData);

// 입력 폼 상태 초기화
// initialData가 있으면 그 값으로 채우고, 없으면 빈 값으로 시작
const form = ref<CreateRoomReq>({
  title: props.initialData?.title || '',
  description: props.initialData?.description || '',
  type: props.initialData?.type || 'PUBLIC',
  password: '' // 비밀번호는 보안상 불러오지 않고, 수정 시에만 새로 입력받음
});

// 폼 제출 핸들러 (분기 처리 핵심 로직!)
const handleSubmit = async () => {
  // A. 유효성 검사
  if (!form.value.title.trim()) {
    return alert('방 제목을 입력해주세요! 📝');
  }
  if (form.value.title.length > 20) {
    return alert('방 제목은 20자 이내로 입력해주세요!');
  }

  // 비공개 방 생성 시 비밀번호 필수
  if (!isEditMode.value && form.value.type === 'PRIVATE' && !form.value.password?.trim()) {
    return alert('비공개 방은 비밀번호가 꼭 필요해요! 🔒');
  }

  let success = false;

  // B. 분기 처리 (Create vs Update)
  if (isEditMode.value && props.initialData?.roomId) {
    // ✏️ 수정 모드
    success = await updateRoom(props.initialData.roomId, form.value);
  } else {
    // ➕ 생성 모드
    success = await createRoom(form.value);
  }
  
  // C. 성공 시 처리
  if (success) {
    emit('success'); // 목록 새로고침 요청
    emit('close');   // 모달 닫기
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
          <!-- 마름모 점선 X 패턴 - 중심 기준 대칭 -->
          <!-- 왼쪽 위에서 오른쪽 아래 대각선 -->
          <div class="absolute top-[4px] left-[4px] w-[2px] h-[2px] bg-[var(--color-choco)] rotate-45"></div>
          <div class="absolute top-[8px] left-[8px] w-[2px] h-[2px] bg-[var(--color-choco)] rotate-45"></div>
          <div class="absolute top-[11px] left-[11px] w-[2px] h-[2px] bg-[var(--color-choco)] rotate-45"></div>
          <div class="absolute top-[14px] left-[14px] w-[2px] h-[2px] bg-[var(--color-choco)] rotate-45"></div>
          <div class="absolute top-[18px] left-[18px] w-[2px] h-[2px] bg-[var(--color-choco)] rotate-45"></div>
          <!-- 오른쪽 위에서 왼쪽 아래 대각선 -->
          <div class="absolute top-[4px] left-[18px] w-[2px] h-[2px] bg-[var(--color-choco)] rotate-45"></div>
          <div class="absolute top-[8px] left-[14px] w-[2px] h-[2px] bg-[var(--color-choco)] rotate-45"></div>
          <div class="absolute top-[11px] left-[11px] w-[2px] h-[2px] bg-[var(--color-choco)] rotate-45"></div>
          <div class="absolute top-[14px] left-[8px] w-[2px] h-[2px] bg-[var(--color-choco)] rotate-45"></div>
          <div class="absolute top-[18px] left-[4px] w-[2px] h-[2px] bg-[var(--color-choco)] rotate-45"></div>
        </div>
      </button>

      <!-- 공개/비공개 토글 삭제 -->

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
            maxlength="20"
            autofocus
          />
          <div class="flex justify-between items-center mt-[-0.25rem] px-1">
            <span class="text-xs text-[var(--color-syrup)]">최대 20자까지 입력할 수 있어요.</span>
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
        <div class="flex items-center gap-2 mt-[-0.5rem]">
          <input
            type="checkbox"
            id="privateRoomCheckbox"
            :checked="form.type === 'PRIVATE'"
            :disabled="isEditMode"
            @change="form.type = ($event.target as HTMLInputElement).checked ? 'PRIVATE' : 'PUBLIC'"
            class="w-4 h-4 accent-[var(--color-butter)] border-2 border-[var(--color-choco)] rounded focus:ring-2 focus:ring-[var(--color-butter)]"
          />
          <label for="privateRoomCheckbox" class="text-[var(--color-choco)] font-['PfStardust30S'] text-[1rem] select-none cursor-pointer">
            비공개방으로 설정
          </label>
        </div>

        <transition name="slide-fade">
          <div v-if="form.type === 'PRIVATE'" class="flex flex-col gap-2">
            <label class="text-[var(--color-choco)] font-['PfStardust30S'] text-[1.125rem] pl-2">
              비밀번호 {{ isEditMode ? '(변경 시 입력)' : '' }}
            </label>
            <input 
              v-model="form.password" 
              type="password" 
              placeholder="비밀번호 4자리 이상" 
              class="input-box"
            />
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