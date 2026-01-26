package com.ssafy.virtudy.member.domain;

public enum JobType {
    SCHOOL_STUDENT, // 학생 (초/중/고)
    UNIVERSITY_STUDENT, // 대학생/대학원생
    JOB_SEEKER, // 취업준비생
    OFFICE_WORKER // 직장인
}
/**
 * FE 코드
 * case '학생 (초/중/고)':
 *           return 'SCHOOL_STUDENT';
 *         case '대학생/대학원생':
 *           return 'UNIVERSITY_STUDENT';
 *         case '취업준비생':
 *           return 'JOB_SEEKER';
 *         case '직장인':
 *           return 'OFFICE_WORKER';
 *         case '기타':
 */