<template>
  <div class="mypage-layout">
    <aside class="sidebar">
      <div class="back-btn" @click="goBack">←</div>
      <h1 class="page-title">마이<br>페이지</h1>
      
      <div class="nav-buttons">
        <button 
          :class="{ active: activeTab === 'profile' }" 
          @click="activeTab = 'profile'"
        >
          프로필
        </button>
        <button 
          :class="{ active: activeTab === 'report' }" 
          @click="activeTab = 'report'"
        >
          리포트
        </button>
      </div>
    </aside>

    <main class="content-area">
      <div v-if="activeTab === 'profile'" class="profile-container">
        
        <button class="edit-btn-top" @click="openEditModal">회원정보수정</button>

        <div class="profile-card">
          <div class="avatar-section">
            <img 
              v-if="userInfo?.avatarImageUrl" 
              :src="userInfo.avatarImageUrl" 
              class="main-avatar"
            />
            <div v-else class="avatar-placeholder">ME</div>
          </div>
          
          <div class="info-section">
            <h2 class="nickname">{{ userInfo?.nickName || '닉네임' }}</h2>
            <p class="sub-info">
              {{ userInfo?.cumulativeScore }}p <span class="divider">|</span> {{ userInfo?.tier }}
            </p>
            <p class="favorite-room">&lt;{{ userInfo?.favoriteStudyRoom }}&gt;</p>
          </div>
        </div>

        <MiniReport />

      </div>

      <div v-else class="report-tab-placeholder">
        <h2>상세 리포트 페이지 (준비중)</h2>
      </div>
    </main>

    <ProfileEditModal 
      v-if="isEditModalOpen"
      :email="userInfo?.email || ''"
      v-model:nickName="editForm.nickName"
      v-model:jobType="editForm.jobType"
      :jobOptions="JOB_OPTIONS"
      @close="closeEditModal"
      @submit="submitEdit"
    />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useMyPage } from '../logic/useMyPage';
import { JOB_OPTIONS } from '../types/mypage.types';
import MiniReport from '../ui/MiniReport.vue';
import ProfileEditModal from '../ui/ProfileEditModal.vue';

const router = useRouter();
const { 
  userInfo, activeTab, isEditModalOpen, editForm, 
  openEditModal, closeEditModal, submitEdit 
} = useMyPage();

const goBack = () => {
  router.push({ name: 'user' });
};
</script>

<style scoped>
.mypage-layout {
  display: flex;
  min-height: 100vh;
  background-color: #FFF5E0; /* 전체 배경 */
  padding: 40px;
}

/* 왼쪽 사이드바 */
.sidebar {
  width: 200px;
  margin-right: 40px;
}
.back-btn {
  font-size: 2rem; cursor: pointer; color: #5A4632; margin-bottom: 20px;
}
.page-title {
  font-size: 3rem; color: #D6B48D; line-height: 1.2; margin-bottom: 40px;
}
.nav-buttons button {
  display: block; width: 100%; padding: 15px; margin-bottom: 10px;
  border: 2px solid #5A4632; background: #FFF; color: #5A4632;
  font-weight: bold; cursor: pointer; border-radius: 8px;
}
.nav-buttons button.active {
  background: #D6B48D; color: white;
}

/* 오른쪽 컨텐츠 */
.content-area {
  flex: 1;
  max-width: 600px;
}
.profile-container {
  background-color: #8B6E4E; /* 컨테이너 박스 배경 */
  border-radius: 20px;
  padding: 30px;
  position: relative;
  box-shadow: 5px 5px 15px rgba(0,0,0,0.1);
  border: 4px solid #5A4632;
}
.edit-btn-top {
  position: absolute; top: 20px; right: 20px;
  background: #FFF5E0; border: 1px solid #5A4632;
  padding: 5px 10px; border-radius: 15px; cursor: pointer; font-size: 0.8rem;
}

.profile-card {
  text-align: center; margin-bottom: 20px; color: #F0E0C0;
}
.avatar-section {
  display: flex; justify-content: center; margin-bottom: 15px;
}
.main-avatar, .avatar-placeholder {
  width: 100px; height: 100px; border-radius: 50%; border: 3px solid #5A4632;
  object-fit: cover; background: #ccc; display: flex; align-items: center; justify-content: center;
}
.nickname { font-size: 1.5rem; margin-bottom: 5px; }
.sub-info { font-size: 0.9rem; color: #D6B48D; margin-bottom: 5px; }
.divider { margin: 0 5px; }
.favorite-room { font-size: 0.9rem; color: #aaa; }
</style>