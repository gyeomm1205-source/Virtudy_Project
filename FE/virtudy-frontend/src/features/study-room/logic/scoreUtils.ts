// src/shared/logic/scoreUtils.ts

/**
 * AI 점수에 따른 하트 색상 반환
 * - 80점 이상: 초록 (#2ed573)
 * - 60점 초과 ~ 80점 미만: 노랑 (#ffa502)
 * - 60점 이하: 빨강 (#ff4757)
 */
export const getScoreColor = (score: number): string => {
    if (score >= 80) {
        return '#2ed573'; // 💚 초록
    } else if (score > 60) {
        return '#ffa502'; // 💛 노랑
    } else {
        return '#ff4757'; // ❤️ 빨강
    }
};
