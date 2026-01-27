<template>
  <div class="user-page-container">
    <section class="right-section">
      
      <div class="ranking-box">
        <h3 class="box-title">개인 랭킹</h3>
        <div v-if="isLoading" class="loading-text">랭킹 불러오는 중...</div>
        <ul v-else class="rank-list">
          
          <li v-for="(item, index) in privateTop5" :key="item.email" class="rank-item">
            <span class="rank-badge" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
            <span class="rank-name">{{ item.nickName }}</span>
            <span class="rank-score">{{ item.score }}p</span>
            <span class="crown-icon" v-if="index < 3">👑</span>
          </li>

        </ul>
      </div>

      <div class="ranking-box mt-20">
        <h3 class="box-title">팀 랭킹</h3>
        <div v-if="isLoading" class="loading-text">랭킹 불러오는 중...</div>
        <ul v-else class="rank-list">
          
          <li v-for="(item, index) in teamTop5" :key="item.rank" class="rank-item">
            <span class="rank-badge" :class="'rank-' + (index + 1)">{{ index + 1 }}</span>
            <span class="rank-name">{{ item.nickName }}</span>
            <span class="rank-score">{{ item.score }}p</span>
            <span class="crown-icon" v-if="index < 3">👑</span>
          </li>

        </ul>
      </div>

    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useMainRanking } from '@/features/ranking/logic/useMainRanking';

// 로직 훅 실행. 이제 훅 자체는 API를 호출하지 않습니다.
const { privateTop5, teamTop5, isLoading, fetchTopRanks } = useMainRanking();

// onMounted 훅을 사용해, 컴포넌트가 완전히 준비된 후 API를 호출합니다.
onMounted(() => {
  fetchTopRanks();
});
</script>

<style scoped>
/* 전체 레이아웃 */
.user-page-container {
  display: flex;
  gap: 40px;
  padding: 40px;
  background-color: #FFF5E0; /* 전체 배경색 (연한 베이지) */
  min-height: 100vh;
  justify-content: center;
}

.left-section {
  flex: 1;
  max-width: 400px;
}

.right-section {
  flex: 1;
  max-width: 450px; /* 랭킹 박스 너비 제한 */
}

/* 랭킹 박스 스타일 (갈색 테두리 박스) */
.ranking-box {
  background-color: #8B6E4E; /* 진한 갈색 배경 */
  border: 4px solid #5A4632; /* 더 진한 테두리 */
  border-radius: 12px;
  padding: 20px;
  color: #F0E0C0; /* 글자색 (연한 아이보리) */
  box-shadow: 6px 6px 0px rgba(90, 70, 50, 0.5); /* 입체감 그림자 */
}

.box-title {
  margin: 0 0 15px 0;
  font-size: 1.4rem;
  color: #D6B48D; /* 제목 색상 */
  font-weight: bold;
  /* 픽셀 폰트 느낌을 원하시면 font-family 추가 */
}

.loading-text {
  text-align: center;
  padding: 20px;
  color: #ccc;
}

/* 리스트 스타일 */
.rank-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.rank-item {
  display: flex;
  align-items: center;
  background-color: #6B5540; /* 리스트 아이템 배경 (약간 밝은 갈색) */
  margin-bottom: 8px;
  padding: 10px 15px;
  border-radius: 8px;
  font-size: 1rem;
  transition: transform 0.2s;
}

.rank-item:hover {
  transform: translateX(5px); /* 마우스 올리면 살짝 움직임 */
}

/* 1, 2, 3위 강조 스타일 */
.rank-item:nth-child(-n+3) {
  background-color: #5A4632; /* 상위권은 더 진한 배경 */
  border: 1px solid #D6B48D;
  font-weight: bold;
}

.rank-badge {
  font-weight: bold;
  margin-right: 12px;
  width: 24px;
  text-align: center;
  font-size: 1.1rem;
}

/* 1,2,3등 숫자 색상 다르게 (선택사항) */
.rank-1 { color: #FFD700; } /* 금 */
.rank-2 { color: #C0C0C0; } /* 은 */
.rank-3 { color: #CD7F32; } /* 동 */

.rank-name {
  flex: 1; /* 이름이 남은 공간 차지 */
  overflow: hidden;
  text-overflow: ellipsis; /* 이름 길면 ... 처리 */
  white-space: nowrap;
}

.rank-score {
  font-size: 0.9rem;
  color: #D6B48D;
  margin-right: 10px;
}

.crown-icon {
  font-size: 1.1rem;
}

.mt-20 {
  margin-top: 25px; /* 박스 사이 간격 */
}

/* (임시) 왼쪽 프로필 영역 플레이스홀더 */
.profile-placeholder {
  background-color: #D6B48D;
  height: 400px;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #5A4632;
}
</style>