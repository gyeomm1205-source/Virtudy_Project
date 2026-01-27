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
              {{ userInfo?.tierScore }}p <span class="divider">|</span> {{ userInfo?.tier }}
            </p>
            <p class="favorite-room">&lt;{{ userInfo?.favoriteRoomTitle }}&gt;</p>
          </div>
        </div>

        <div class="mini-report-section">
          <h3 class="section-title">미니 리포트</h3>
          
          <MiniReport 
            :studyTime="userInfo?.pureStudyTime" 
            :focusing="userInfo?.focusDepth" 
          />
        </div>
        <PentagonChart />
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
import MiniReport from '@/shared/ui/MiniReport.vue';
import PentagonChart from '@/shared/ui/PentagonChart.vue';  
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

