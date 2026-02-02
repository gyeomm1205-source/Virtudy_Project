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
    
    <div class="bg-white w-[480px] rounded-[30px] border-4 border-[var(--color-choco)] p-8 shadow-xl transform transition-all relative">
      
      <button 
        class="absolute top-4 right-4 text-[var(--color-choco)] hover:scale-110 transition-transform"
        @click="$emit('close')"
      >
        <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="3">
          <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>

      <h2 class="text-[var(--color-choco)] text-[32px] font-['Ram'] text-center mb-6">
        {{ isEditMode ? '방 정보 수정' : '방 만들기' }}
      </h2>

      <div class="flex flex-col gap-5">
        
        <div class="flex flex-col gap-2">
          <label class="text-[var(--color-choco)] font-bold text-lg pl-2">방 제목</label>
          <input 
            v-model="form.title" 
            type="text" 
            placeholder="예) 알고리즘 뿌시기 👊" 
            class="input-box"
            autofocus
          />
        </div>

        <div class="flex flex-col gap-2">
          <label class="text-[var(--color-choco)] font-bold text-lg pl-2">설명</label>
          <textarea 
            v-model="form.description" 
            placeholder="어떤 스터디인지 알려주세요!" 
            class="input-box h-24 resize-none"
          ></textarea>
        </div>

        <div class="flex gap-4">
          <template v-if="isEditMode">
            <div
              class="flex-1 p-3 rounded-xl border-2 text-center font-bold bg-[var(--color-butter)] border-[var(--color-choco)] text-[var(--color-choco)]"
            >
              {{ form.type === 'PUBLIC' ? '📢 공개' : '🔒 비공개' }}
            </div>
          </template>
          <template v-else>
            <label 
              class="flex-1 cursor-pointer p-3 rounded-xl border-2 transition-all text-center font-bold"
              :class="form.type === 'PUBLIC' 
                ? 'bg-[var(--color-butter)] border-[var(--color-choco)] text-[var(--color-choco)]' 
                : 'bg-gray-100 border-gray-300 text-gray-400'"
            >
              <input type="radio" v-model="form.type" value="PUBLIC" class="hidden">
              📢 공개
            </label>
            <label 
              class="flex-1 cursor-pointer p-3 rounded-xl border-2 transition-all text-center font-bold"
              :class="form.type === 'PRIVATE' 
                ? 'bg-[var(--color-butter)] border-[var(--color-choco)] text-[var(--color-choco)]' 
                : 'bg-gray-100 border-gray-300 text-gray-400'"
            >
              <input type="radio" v-model="form.type" value="PRIVATE" class="hidden">
              🔒 비공개
            </label>
          </template>
        </div>

        <transition name="slide-fade">
          <div v-if="form.type === 'PRIVATE'" class="flex flex-col gap-2">
            <label class="text-[var(--color-choco)] font-bold text-lg pl-2">
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

      <div class="mt-8 flex gap-3">
        <button 
          class="flex-1 py-3 rounded-xl bg-gray-200 text-gray-500 font-bold hover:bg-gray-300 transition-colors"
          @click="$emit('close')"
        >
          취소
        </button>
        <button 
          class="flex-1 py-3 rounded-xl bg-[var(--color-choco)] text-white font-['Xcu'] text-xl font-bold hover:bg-[#3A2A2A] transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          @click="handleSubmit"
          :disabled="isLoading"
        >
          {{ isLoading ? '처리 중...' : (isEditMode ? '수정 완료!' : '만들기 완료!') }}
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
  border: 3px solid #E5E7EB;
  border-radius: 16px;
  font-size: 16px;
  color: var(--color-choco);
  transition: all 0.2s;
  background-color: #F9FAFB;
}

/* 포커스 시 초코색 테두리 */
.input-box:focus {
  border-color: var(--color-choco);
  outline: none;
  background-color: white;
}

/* 플레이스홀더 색상 */
.input-box::placeholder {
  color: #9CA3AF;
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
</style>