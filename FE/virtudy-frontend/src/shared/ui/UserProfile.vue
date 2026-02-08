<template>
  <div class="user-profile-card shadow-[4px_4px_0px_0px_var(--color-choco)]">
    <div class="profile-main">
      
      <div class="profile-image-container" @click="handleProfileClick">
        
        <template v-if="avatarCreateLimitReached">
          <div class="absolute -top-6 left-0 w-full text-center text-red-500 font-bold text-sm">
            오늘 생성 기회 소진!
          </div>
        </template>

        <div class="w-[9rem] h-[9rem] rounded-full bg-[var(--color-butter)] relative overflow-hidden shadow-md mx-auto mt-[1.5rem] transform-gpu">
          
          <CharacterAvatar 
            v-if="hasAvatarConfig" 
            :config="avatar!" 
            :offset-x="avatarOffsetX"
            :offset-y="avatarOffsetY"
            class="absolute inset-0 w-full h-full object-cover scale-[1.45] origin-center translate-y-[5%]"
          />
          
          <img 
            v-else-if="avatarImageUrl" 
            :src="avatarImageUrl" 
            alt="프로필 이미지"
            class="absolute inset-0 w-full h-full object-cover scale-110 origin-center"
          />
          
          <div v-else class="w-full h-full flex flex-col items-center justify-center hover:bg-[#FFE08C] transition-colors cursor-pointer group">
            <span class="text-[var(--color-choco)] font-bold text-[1.1rem] font-['Xcu'] leading-tight text-center">
              아바타<br>생성하기
            </span>
          </div>

        </div>
      </div>
      
      <div class="nickname-area">
        <p class="nickname-text">
          {{ nickName }}
        </p>
      </div>
      
      <div class="score-tier-area">
        <p class="info-text">
          {{ tierScore }} p
        </p>
        <p class="info-text">
          {{ tier }}
        </p>
      </div>
      
      <div class="fav-study-area">
        <p class="fav-text">
          <template v-if="favoriteRoomTitle">
            &lt; {{ favoriteRoomTitle }} &gt;
          </template>
          <template v-else>
            ( 최애 스터디 없음 )
          </template>
        </p>
      </div>
    </div>
    
    <div class="stats-area">
      <MiniReport 
       class="w-full h-full gap-[6rem]"
        :studyTime="displayPureStudyTime" 
        :focusing="displayFocusDepth" 
      />
      <!-- 백엔드에서 해결되면 다시 사용해야됨 -->
      <!-- :studyTime="userInfo?.dailyPureStudyTime"
           :focusing="userInfo?.dailyFocusDepth" -->
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import MiniReport from '@/shared/ui/MiniReport.vue'; 
import CharacterAvatar from '@/shared/ui/avatar/CharacterAvatar.vue';
import type { AvatarConfig } from '@/shared/types/common.types';
import { useAuthStore } from '@/stores/authStore';

// 클릭 이벤트
const emit = defineEmits<{
  (e: 'clickProfile'): void
}>();


// 하루 3번 제한 (날짜별 카운트)
import { ref } from 'vue';
const AVATAR_CREATE_LIMIT = 3;
const AVATAR_CREATE_KEY = 'avatarCreateCountByDate';
const avatarCreateLimitReached = ref(false);

function getTodayKey() {
  const today = new Date();
  return today.toISOString().slice(0, 10); // YYYY-MM-DD
}

function getAvatarCreateCount() {
  const data = JSON.parse(localStorage.getItem(AVATAR_CREATE_KEY) || '{}');
  const todayKey = getTodayKey();
  return data[todayKey] || 0;
}

function handleProfileClick() {
  const count = getAvatarCreateCount();
  const remain = AVATAR_CREATE_LIMIT - count;
  if (remain <= 0) {
    avatarCreateLimitReached.value = true;
    return;
  }
  avatarCreateLimitReached.value = false;
  emit('clickProfile');
}

interface UserProfileProps {
  nickName?: string;
  tierScore?: number;       // userScore -> tierScore
  tier?: string;
  favoriteRoomTitle: string; 
  // [중요] 백엔드에서 오는 데이터 이름 그대로 사용
  pureStudyTime?: number;   // studyHours -> pureStudyTime (분 단위 숫자)
  focusDepth?: number;      // concentration -> focusDepth (퍼센트 숫자) 
  avatarImageUrl?: string;  // userProfileImage -> avatarImageUrl
  avatar?: AvatarConfig;    // 백엔드 avatar 설정 값
  avatarOffsetX?: string;   // 아바타 X 오프셋
  avatarOffsetY?: string;   // 아바타 Y 오프셋
}

const props = withDefaults(defineProps<UserProfileProps>(), {
  nickName: "닉네임",
  tierScore: 0,
  tier: "티어명",
  favoriteRoomTitle: "",
  pureStudyTime: 0,
  focusDepth: 0,
  avatarImageUrl: "",
  avatarOffsetX: '-6%',
  avatarOffsetY: '-3%'
});

const authStore = useAuthStore();
const displayPureStudyTime = computed(() => {
  // 백엔드 값이 없으면 임시값 사용
  if (props.pureStudyTime === undefined || props.pureStudyTime === 0) {
    return authStore.userInfo?.tempPureStudyTime ?? 0;
  }
  return props.pureStudyTime;
});
const displayFocusDepth = computed(() => {
  if (props.focusDepth === undefined || props.focusDepth === 0) {
    return authStore.userInfo?.tempFocusDepth ?? 0;
  }
  return props.focusDepth;
});

const avatarOffsetX = computed(() => props.avatarOffsetX || '-15%');
const avatarOffsetY = computed(() => props.avatarOffsetY || '-30%');

const hasAvatarConfig = computed(() => {
  if (!props.avatar) return false;
  return Object.values(props.avatar).some((value) => Boolean(value));
});


</script>

<style scoped>
/* 기존 스타일 유지 */
.user-profile-card {
  background-color: var(--color-syrup);
  border: 2px solid var(--color-choco);
  height: 433px;
  width: 473px;
  border-radius: 20px;
  overflow: hidden;
  position: relative;
}

.profile-main {
  position: relative;
  height: 276px;
  width: 100%;
}

.profile-image-container {
  position: relative;
  width: 100%;
  /* 아바타 컨테이너 위치 잡기 */
}

.nickname-area { position: absolute; top: 150px; width: 100%; text-align: center; }
.nickname-text { color: var(--color-choco); font-size: 28px; font-weight: bold; font-family: 'Xcu', sans-serif; }

.score-tier-area { position: absolute; top: 187px; width: 100%; display: flex; justify-content: center; gap: 20px; }
.info-text { color: var(--color-pancake); font-size: 24px; font-family: 'PfStardust30S', sans-serif; }

.fav-study-area { position: absolute; top: 210px; width: 100%; text-align: center; }
.fav-text { color: var(--color-choco); font-size: 24px; font-family: 'PfStardust30S', sans-serif; }

/* MiniReport 위치 및 스타일 오버라이딩 */
.stats-area {
  position: absolute;
  top: 288px; /* 기존 디자인 위치 */
  left: 13px;
  width: 447px;
  height: 133px;
}

:deep(.study-stats-container) {
  background-color: var(--color-choco);
}

</style>