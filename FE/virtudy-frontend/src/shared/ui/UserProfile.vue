<template>
  <div class="user-profile-card shadow-[4px_4px_0px_0px_var(--color-choco)]">
    <div class="profile-main">
      <div class="profile-image-wrapper">
        <div class="image-container">
          <img 
            :src="profileFrameUrl" 
            alt="프로필 프레임"
            class="frame-img"
          />
          <div class="user-img-box">
            <img 
              :src="avatarImageUrl || defaultProfileImage" 
              alt="프로필 이미지"
              class="user-img"
            />
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
          &lt; {{ favoriteRoomTitle }} &gt;
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
import MiniReport from '@/shared/ui/MiniReport.vue'; 

// [수정] 백엔드 명세와 동일한 필드명으로 Props 정의
interface AvatarConfig {
  hairFront: string;
  hairBack: string;
  hairColor: string;
  eyes: string;
  glasses: string;
  outfit: string;
  clothesColor: string;
}

interface UserProfileProps {
  nickName?: string;
  tierScore?: number;       // userScore -> tierScore
  tier?: string;
  favoriteRoomTitle?: string; // favoriteStudy -> favoriteRoomTitle
  // [중요] 백엔드에서 오는 데이터 이름 그대로 사용
  pureStudyTime?: number;   // studyHours -> pureStudyTime (분 단위 숫자)
  focusDepth?: number;      // concentration -> focusDepth (퍼센트 숫자) 
  avatarImageUrl?: string;  // userProfileImage -> avatarImageUrl
  avatar?: AvatarConfig;    // 백엔드 avatar 설정 값
}

// 기본값 설정 (데이터가 없을 때 보여줄 값)
withDefaults(defineProps<UserProfileProps>(), {
  nickName: "닉네임",
  tierScore: 0,
  tier: "티어명",
  favoriteRoomTitle: "최애스터디이름",
  pureStudyTime: 0,
  focusDepth: 0,
  avatarImageUrl: ""
});

// 이미지 URLs
const profileFrameUrl = "/vite.svg"; // [수정] 임시 플레이스홀더
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

.image-container { position: relative; width: 146px; height: 146px; margin: 0 auto; top: 10px; }
.frame-img { width: 100%; height: 100%; object-fit: cover; }
.user-img-box { position: absolute; top: 6px; left: 8px; width: 130px; height: 130px; border-radius: 50%; overflow: hidden; }
.user-img { width: 100%; height: 100%; object-fit: cover; }

.nickname-area { position: absolute; top: 158px; width: 100%; text-align: center; }
.nickname-text { color: var(--color-choco); font-size: 28px; font-weight: bold; font-family: 'Xcu', sans-serif; }

.score-tier-area { position: absolute; top: 190px; width: 100%; display: flex; justify-content: center; gap: 20px; }
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