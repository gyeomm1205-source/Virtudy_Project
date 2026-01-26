<template>
  <div class="bg-[var(--color-syrup)] border-2 border-[var(--color-choco)] border-solid h-[433px] overflow-clip rounded-[20px] w-[473px]">
    <!-- 프로필 메인 영역 -->
    <div class="relative h-[276px] w-full">
      <!-- 프로필 이미지 -->
      <div class="absolute left-1/2 top-[28px] transform -translate-x-1/2">
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
      
      <!-- 닉네임 -->
      <div class="absolute left-1/2 top-[201px] transform -translate-x-1/2">
        <p class="text-[var(--color-choco)] text-[28px] font-['Xcu'] font-medium leading-none text-center">
          {{ userNickname || "닉네임" }}
        </p>
      </div>
      
      <!-- 점수와 티어 -->
      <div class="absolute left-1/2 top-[230px] transform -translate-x-1/2 flex items-center gap-[20px]">
        <p class="text-[var(--color-pancake)] text-[24px] font-['PfStardust30S'] font-normal leading-none">
          {{ userScore || "000" }} p
        </p>
        <p class="text-[var(--color-pancake)] text-[24px] font-['PfStardust30S'] font-normal leading-none">
          {{ userTier || "티어명" }}
        </p>
      </div>
      
      <!-- 최애 스터디 -->
      <div class="absolute left-1/2 top-[252px] transform -translate-x-1/2">
        <p class="text-[var(--color-choco)] text-[24px] font-['PfStardust30S'] font-normal leading-none text-center">
          {{ favoriteStudy || "<최애스터디이름>" }}
        </p>
      </div>
    </div>
    
    <!-- 통계 영역 -->
    <div class="absolute h-[157px] left-0 top-[274px] w-full overflow-clip">
      <div class="absolute bg-[var(--color-choco)] h-[133px] left-[13px] rounded-[20px] top-[12px] w-[447px]">
        <!-- 공부시간 -->
        <div class="absolute left-[106px] top-1/2 transform -translate-y-1/2">
          <p class="text-[var(--color-cream)] text-[42px] font-['Ram'] font-medium leading-[48px] tracking-[-0.84px] text-center">
            {{ studyHours || "0" }}h
          </p>
          <p class="text-[var(--color-syrup)] text-[24px] font-['PfStardust30S'] font-normal leading-none text-center mt-2">
            공부시간
          </p>
        </div>
        
        <!-- 집중도 -->
        <div class="absolute right-[106px] top-1/2 transform -translate-y-1/2">
          <p class="text-[var(--color-cream)] text-[42px] font-['Ram'] font-medium leading-[48px] tracking-[-0.84px] text-center">
            {{ concentration || "00" }}%
          </p>
          <p class="text-[var(--color-syrup)] text-[24px] font-['PfStardust30S'] font-normal leading-none text-center mt-2">
            집중도
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface UserProfileProps {
  userNickname?: string;
  userScore?: string;
  userTier?: string;
  favoriteStudy?: string;
  studyHours?: string;
  concentration?: string;
  userProfileImage?: string;
}

// Props 정의
const props = withDefaults(defineProps<UserProfileProps>(), {
  userNickname: "닉네임",
  userScore: "000",
  userTier: "티어명",
  favoriteStudy: "<최애스터디이름>",
  studyHours: "0",
  concentration: "00",
});

// 이미지 URLs (Figma에서 제공된 이미지들)
const profileFrameUrl = "http://localhost:3845/assets/66144dc9e39a8373edb406dbf825edc71dbab23b.svg";
const defaultProfileImage = "http://localhost:3845/assets/b8326e9b387ebdb70a5f94f9be04f6872ffd0f1b.png";
</script>