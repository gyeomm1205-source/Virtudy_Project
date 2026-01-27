export interface UserProfileResponse {
  avatarImageUrl: string;
  email: string;
  nickName: string;
  tier: string;
  jobType: string;
  tierScore?: number; // 티어 점수
  favoriteRoomTitle?: string; //최애 스터디방 제목
  pureStudyTime?: number; // 일일 순공부시간 (분)
  focusDepth?: number; // 집중도 (%)
}

export interface ProfileUpdateRequest {
  nickName: string;
  jobType: string;
}

export const JOB_OPTIONS = [
  { value: 'SCHOOL_STUDENT', label: '초/중/고등학생' },
  { value: 'UNIVERSITY_STUDENT', label: '대학생' },
  { value: 'JOB_SEEKER', label: '취업준비생' },
  { value: 'OFFICE_WORKER', label: '직장인' },
  { value: 'ETC', label: '기타' },
];