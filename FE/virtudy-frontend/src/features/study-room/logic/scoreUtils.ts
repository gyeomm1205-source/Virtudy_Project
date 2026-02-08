// src/shared/logic/scoreUtils.ts

/**
 * Returns heart color by score.
 * - 80-100: green (#2ed573)
 * - 60-79: yellow (#ffa502)
 * - 0-59: red (#ff4757)
 */
export const getScoreColor = (score: number): string => {
    if (score > 79) {
        return '#B8D576';
    } else if (score > 59) {
        return '#FFD966';
    } else {
        return '#D70654';
    }
};
