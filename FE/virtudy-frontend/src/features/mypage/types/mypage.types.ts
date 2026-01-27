export interface UserProfileResponse {
  avatarImageUrl: string;
  email: string;
  nickName: string;
  tier: string;
  jobType: string;
  // 백엔드 미구현 필드 (임시)
  cumulativeScore?: number; // 누적 점수
  favoriteStudyRoom?: string; //최애 스터디룸
  miniReport?: string; //미니 리포트
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