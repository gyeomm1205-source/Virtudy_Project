import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useStudyStore = defineStore('study', () => {
  const accessToken = ref<string | null>(sessionStorage.getItem('study_token'));
  const currentRoomId = ref<string | null>(sessionStorage.getItem('study_room_id'));

  const setToken = (token: string, roomId: string) => {
    accessToken.value = token;
    currentRoomId.value = roomId;
    sessionStorage.setItem('study_token', token);
    sessionStorage.setItem('study_room_id', roomId);
  };

  const clearToken = () => {
    accessToken.value = null;
    currentRoomId.value = null;
    sessionStorage.removeItem('study_token');
    sessionStorage.removeItem('study_room_id');
  };

  return {
    accessToken,
    currentRoomId,
    setToken,
    clearToken,
  };
});
