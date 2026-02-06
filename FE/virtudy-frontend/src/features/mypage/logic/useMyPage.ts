import { ref, reactive, onMounted } from 'vue';
import { useAuthStore } from '@/stores/authStore';
import { useUiStore } from '@/stores/uiStore'; // UI 스토어 import
import { getMyProfile, updateMyProfile } from '../api/mypageApi';
import type { UserProfileResponse } from '../types/mypage.types';

export const useMyPage = () => {
  const authStore = useAuthStore();
  const uiStore = useUiStore(); // 스토어 사용

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
      const hasAvatarConfig = data.avatar && Object.values(data.avatar).some((value) => Boolean(value));
      const mergedAvatar = hasAvatarConfig ? data.avatar : authStore.userInfo?.avatar;
      userInfo.value = {
        ...data,
        avatar: mergedAvatar,
      };

      // 스토어 정보도 최신화 (헤더 등 전역 반영을 위해)
      if (authStore.userInfo) {
        authStore.setUserInfo({
          ...authStore.userInfo,
          nickName: data.nickName,
          avatar: mergedAvatar,
          avatarImageUrl: data.avatarImageUrl ?? authStore.userInfo.avatarImageUrl,
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
      
      await uiStore.openAlert('회원정보가 수정되었습니다.', '성공');
      
      await fetchProfile(); // 최신 정보 다시 로드
      closeEditModal();
    } catch (error) {
      console.error('수정 실패:', error);
      await uiStore.openAlert('정보 수정에 실패했습니다.', '오류');
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