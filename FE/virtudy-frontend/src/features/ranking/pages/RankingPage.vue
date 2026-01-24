<template>
  <div class="ranking-container">
    <header class="ranking-header">
      <div class="back-button" @click="goBack">←</div>
      <div class="header-content">
        <h1>랭킹</h1>
        <p class="info-text">※ 랭킹은 매일 자정(00:00)에 갱신됩니다.</p>
        <div class="my-rank-info" v-if="myRankInfo">
           <div class="avatar-circle">
             <img v-if="myRankInfo.profileImg" :src="myRankInfo.profileImg" />
             <span v-else>Me</span>
           </div>
           <div class="text-info">
             <p v-if="authStore.userInfo">{{ authStore.userInfo.nickName }} {{ rankType === 'private' ? '님의 순위' : '팀의 순위' }}</p>
             <p v-else>{{ myRankInfo.id }} {{ rankType === 'private' ? '님의 순위' : '팀의 순위' }}</p>
             <p class="rank-number">{{ myRankInfo.rank }}위</p>
           </div>
        </div>
      </div>
    </header>

    <div class="controls-section">
      <div class="tabs">
        <button :class="{ active: rankType === 'private' }" @click="changeType('private')">개인</button>
        <button :class="{ active: rankType === 'team' }" @click="changeType('team')">팀</button>
      </div>
      <div class="search-bar">
        <input v-model="searchKeyword" @keyup.enter="handleSearch" placeholder="검색..." />
        <button @click="handleSearch">🔍</button>
      </div>
    </div>

    <div class="ranking-board">
      <div v-if="isLoading">Loading...</div>
      <div v-else class="rank-list">
        <div v-for="item in rankList" :key="item.id" class="rank-item">
          <span class="col-rank">{{ item.rank }}</span>
          <span class="col-name">{{ item.id }}</span>
          <span class="col-score">{{ item.score }}p</span>
          <span class="col-tier">{{ item.tier }}</span>
        </div>
      </div>

      <div class="pagination">
        <button 
          @click="changePage(Math.max(0, currentPage - 5))" 
          :disabled="currentPage === 0"
          class="nav-btn"
        >
          &lt;&lt;
        </button>

        <button 
          @click="changePage(currentPage - 1)" 
          :disabled="currentPage === 0"
          class="nav-btn"
        >
          &lt;
        </button>

        <button 
          v-for="page in visiblePages" 
          :key="page" 
          :class="{ active: currentPage === page }"
          @click="changePage(page)"
        >
          {{ page + 1 }}
        </button>

        <button 
          @click="changePage(currentPage + 1)" 
          :disabled="currentPage >= totalPages - 1"
          class="nav-btn"
        >
          &gt;
        </button>

        <button 
          @click="changePage(Math.min(totalPages - 1, currentPage + 5))" 
          :disabled="currentPage >= totalPages - 1"
          class="nav-btn"
        >
          &gt;&gt;
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '../../../stores/authStore';
// 로직 파일만 import하면 끝!
import { useRanking } from '../logic/useRanking';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();

// logic 폴더에 짜둔 코드를 그대로 불러오기
const { 
  rankType, searchKeyword, rankList, myRankInfo, isLoading,
  currentPage, visiblePages, totalPages,
  changeType, changePage, handleSearch 
} = useRanking();

// 뒤로 가기 함수
const goBack = () => {
  router.push({ name: 'user' }); 
};
</script>

<style scoped>
.ranking-container {
  background-color: #D6B48D; 
  color: #5A4632;
  padding: 20px;
  min-height: 100vh;
}

.ranking-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 3rem;
  margin-right: 30px;
  color: #C0A080; 
  text-shadow: 2px 2px #5A4632;
}

.my-rank-info {
  display: flex;
  align-items: center;
}

.avatar-circle {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #5A4632;
  margin-right: 15px;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
}

.avatar-circle img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.text-info .nickname {
  font-size: 1.2rem;
}

.text-info .highlight {
  font-weight: bold;
  font-size: 1.3rem;
}

.text-info .rank-number {
  font-size: 2.5rem;
  font-weight: bold;
  color: #F4E0A0; 
  text-shadow: 1px 1px #5A4632;
}

.controls-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 10px;
}

.tabs {
  display: flex;
  gap: 5px;
}

.tab-btn {
  background-color: #F0E0C0;
  border: 2px solid #5A4632;
  border-bottom: none;
  padding: 10px 20px;
  cursor: pointer;
  font-weight: bold;
  border-radius: 10px 10px 0 0;
}

.tab-btn.active {
  background-color: #5A4632;
  color: #F0E0C0;
}

.search-bar {
  display: flex;
  border: 2px solid #5A4632;
  background-color: #FFF;
  border-radius: 5px;
  padding: 5px;
}

.search-bar input {
  border: none;
  outline: none;
  padding: 5px;
}

.ranking-board {
  background-color: #5A4632;
  border-radius: 15px;
  padding: 20px;
  color: #FFF5E0;
}

.rank-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #7A6652;
  background-color: #6B5540; /* 리스트 배경색 */
  margin-bottom: 5px;
  border-radius: 5px;
}

.rank-item:nth-child(odd) {
  background-color: #634E3A; /* 줄무늬 효과 */
}

/* 1,2,3위 강조 */
.rank-item.top-tier {
  background-color: #FFE4B5;
  color: #5A4632;
  font-weight: bold;
}

.col-rank { width: 10%; text-align: center; font-size: 1.2rem; }
.col-name { width: 50%; text-align: left; }
.col-score { width: 20%; text-align: right; }
.col-tier { width: 10%; text-align: center; }

.pagination {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-top: 20px;
}

.pagination button {
  background: none;
  border: none;
  color: #A08060;
  cursor: pointer;
  font-size: 1.2rem;
}

.pagination button.active {
  color: #FFF;
  font-weight: bold;
  text-decoration: underline;
}

.pagination button:disabled {
  color: #555;
  cursor: not-allowed;
}
</style>
