<template>
  <div class="ranking-container">
    <header class="ranking-header">
      <div class="back-button" @click="goBack">←</div>
      <div class="header-content">
        <h1>랭킹</h1>
        <p class="info-text">※ 랭킹은 매일 자정(00:00)에 갱신됩니다.</p>
        
        <div class="my-rank-info" v-if="myRankInfo">
           <div class="avatar-circle">
             <span>Me</span>
           </div>
           <div class="text-info">
             <p>
               <span class="highlight">{{ myRankInfo.nickName }}</span>
               {{ rankType === 'private' ? '님의 순위' : '팀의 순위' }}
             </p>
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
        
        <div 
          v-for="item in rankList" 
          :key="item.email" 
          class="rank-item"
          :class="{ 'is-me': isMyself(item) }"
        >
          <span class="col-rank">{{ item.rank }}</span>
          
          <span class="col-name">
            {{ item.nickName }}
            <span v-if="isMyself(item)" class="me-badge">(나)</span>
          </span>
          
          <span class="col-score">{{ item.score }}p</span>
          <span class="col-tier">{{ item.tier }}</span>
        </div>

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
</template>

<script setup lang="ts">
import { useAuthStore } from '../../../stores/authStore';
import { useRanking } from '../logic/useRanking';
import { useRouter } from 'vue-router';
// [수정] 타입 임포트 추가
import type { RankItem } from '../types/ranking.types';

const authStore = useAuthStore();
const router = useRouter();

const { 
  rankType, searchKeyword, rankList, myRankInfo, isLoading,
  currentPage, visiblePages, totalPages,
  changeType, changePage, handleSearch 
} = useRanking();

const goBack = () => {
  router.push({ name: 'user' }); 
};

// [수정] 본인 확인 함수 구현
// 리스트 아이템의 이메일과 로그인한 유저의 이메일을 비교합니다.
const isMyself = (item: RankItem) => {
  if (!authStore.userInfo?.email) return false;
  return item.email === authStore.userInfo.email;
};
</script>

<style scoped>
.ranking-container {
  background-color: #FFF5E0; 
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

/* [수정] 본인일 경우 강조 스타일 */
.rank-item.is-me {
  background-color: #5A4632; /* 배경 진하게 */
  border: 2px solid #FFD700; /* 금색 테두리 */
}

.rank-item.is-me .col-name {
  font-weight: 900;           /* 글씨 두껍게 */
  color: #FFD700;             /* 글씨 금색 */
  text-decoration: underline; /* 밑줄 */
}

.me-badge {
  font-size: 0.8rem;
  color: #FF6B6B;
  margin-left: 5px;
  text-decoration: none;
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