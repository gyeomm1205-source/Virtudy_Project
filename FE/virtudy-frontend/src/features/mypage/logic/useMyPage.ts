import { ref, reactive, onMounted } from 'vue';
import { useAuthStore } from '@/stores/authStore';
import { getMyProfile, updateMyProfile } from '../api/mypageApi';
import type { UserProfileResponse } from '../types/mypage.types';

export const useMyPage = () => {
  const authStore = useAuthStore();
  
  // 상태
  const userInfo = ref<UserProfileResponse | null>(null);
  const isLoading = ref(false);
  const isEditModalOpen = ref(false);
  const activeTab = ref<'profile' | 'report'>('profile');

  // 수정 폼 데이터
  const editForm = reactive({
    nickName: '',
    jobType: '',
  });

  // 데이터 불러오기
  const fetchProfile = async () => {
    isLoading.value = true;
    try {
      const data = await getMyProfile();
      // 백엔드가 준 이름으로 변경
      userInfo.value = {
        ...data,
        tierScore: 1250, // 임시 점수
        favoriteRoomTitle: '최애스터디룸', // 임시 방이름
        pureStudyTime: 180, // 일일 순공부시간
        focusDepth: 85, // 임시 집중도 (%)
      };
      
      // 스토어 정보도 최신화 (헤더 등 전역 반영을 위해)
      if (authStore.userInfo) {
        authStore.setUserInfo({ 
          ...authStore.userInfo, 
          nickName: data.nickName,
          // avatarImageUrl 등이 바뀌었다면 여기서 업데이트
        });
      }
    } catch (error) {
      console.error('프로필 로딩 실패:', error);
    } finally {
      isLoading.value = false;
    }
  };

  // 모달 열기 (현재 정보 복사)
  const openEditModal = () => {
    if (userInfo.value) {
      editForm.nickName = userInfo.value.nickName;
      editForm.jobType = userInfo.value.jobType || 'SCHOOL_STUDENT';
    }
    isEditModalOpen.value = true;
  };

  // 모달 닫기
  const closeEditModal = () => {
    isEditModalOpen.value = false;
  };

  // 수정 요청
  const submitEdit = async () => {
    try {
      await updateMyProfile({
        nickName: editForm.nickName,
        jobType: editForm.jobType,
      });
      alert('회원정보가 수정되었습니다.');
      await fetchProfile(); // 최신 정보 다시 로드
      closeEditModal();
    } catch (error) {
      console.error('수정 실패:', error);
      alert('정보 수정에 실패했습니다.');
    }
  };

  onMounted(() => {
    fetchProfile();
  });

  return {
    userInfo,
    isLoading,
    activeTab,
    isEditModalOpen,
    editForm,
    fetchProfile,
    openEditModal,
    closeEditModal,
    submitEdit,
  };
};