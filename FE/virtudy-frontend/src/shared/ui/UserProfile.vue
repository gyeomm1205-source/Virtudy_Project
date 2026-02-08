<template>
  <div class="user-profile-card shadow-[4px_4px_0px_0px_var(--color-choco)]">
    <div class="profile-main">
      <div class="profile-image-wrapper" @click="handleProfileClick">
        <template v-if="avatarCreateLimitReached">
          <div style="color: red; font-weight: bold; text-align: center; margin-bottom: 8px;">
            아바타 생성 기회를 모두 사용하셨습니다.
          </div>
        </template>
        <div class="relative w-[9rem] h-[7.5rem] mb-[0.5rem]">
          <div class="w-[10.5rem] h-[13rem]">
            <CharacterAvatar 
              v-if="hasAvatarConfig" 
              :config="avatar!" 
              :offset-x="avatarOffsetX"
              :offset-y="avatarOffsetY"
              class="w-full h-full"
            />
            <img 
              v-else-if="avatarImageUrl" 
              :src="avatarImageUrl" 
              alt="프로필 이미지"
              class="w-full h-full object-cover"
            />
            <div v-else class="w-[9rem] h-[9rem] mx-auto translate-x-[9.5rem] translate-y-[0.7rem] rounded-full bg-[var(--color-butter)] flex items-center justify-center text-[1.25rem] font-bold text-[var(--color-choco)] text-center font-['Xcu']">
              아바타 생성하기
            </div>
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
        :studyTime="pureStudyTime" 
        :focusing="focusDepth" 
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import MiniReport from '@/shared/ui/MiniReport.vue'; 
import CharacterAvatar from '@/shared/ui/avatar/CharacterAvatar.vue';
import type { AvatarConfig } from '@/shared/types/common.types';


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

// 기본값 설정 (데이터가 없을 때 보여줄 값)
const props = withDefaults(defineProps<UserProfileProps>(), {
  nickName: "닉네임",
  tierScore: 0,
  tier: "티어명",
  favoriteRoomTitle: "",
  pureStudyTime: 0,
  focusDepth: 0,
  avatarImageUrl: "",
  avatarOffsetX: '75%',
  avatarOffsetY: '-20%'
});

const avatarOffsetX = computed(() => props.avatarOffsetX || '-15%');
const avatarOffsetY = computed(() => props.avatarOffsetY || '-30%');

const hasAvatarConfig = computed(() => {
  if (!props.avatar) return false;
  return Object.values(props.avatar).some((value) => Boolean(value));
});

// 이미지 URLs
const defaultProfileImage = "/vite.svg"; // [수정] 임시 플레이스홀더
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

/* 클릭 가능한 영역 스타일 */
.profile-image-wrapper { cursor: pointer; }
.profile-image-wrapper:hover { transform: scale(1.05); transition: transform 0.2s ease; }

.nickname-area { position: absolute; top: 160px; width: 100%; text-align: center; }
.nickname-text { color: var(--color-choco); font-size: 28px; font-weight: bold; font-family: 'Xcu', sans-serif; }

.score-tier-area { position: absolute; top: 196px; width: 100%; display: flex; justify-content: center; gap: 20px; }
.info-text { color: var(--color-pancake); font-size: 24px; font-family: 'PfStardust30S', sans-serif; }

.fav-study-area { position: absolute; top: 218px; width: 100%; text-align: center; }
.fav-text { color: var(--color-choco); font-size: 24px; font-family: 'PfStardust30S', sans-serif; }

/* MiniReport 위치 및 스타일 오버라이딩 */
.stats-area {
  position: absolute;
  top: 260px; /* 기존 디자인 위치 */
  left: 13px;
  width: 447px;
  height: 133px;
}

:deep(.study-stats-container) {
  background-color: var(--color-choco);
  border-radius: 20px;
  height: 100%;
  width: 100%;
  padding: 0; /* 내부 패딩 조정 */
  display: flex;
  align-items: center;
}

:deep(.value) {
  color: var(--color-cream);
  font-size: 42px;
  font-family: 'Ram', sans-serif;
}

:deep(.label) {
  color: var(--color-syrup);
  font-size: 24px;
  font-family: 'PfStardust30S', sans-serif;
  margin-top: 5px;
}
</style>