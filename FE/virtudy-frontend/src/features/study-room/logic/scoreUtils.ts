export const getScoreColor = (score: number) => {
    if (score >= 80) {
        return '#2ed573'; // 💚 초록 (80점 이상)
    } else if (score > 60) {
        return '#ffa502'; // 💛 노랑 (60점 초과 ~ 80점 미만)
    } else {
        return '#ff4757'; // ❤️ 빨강 (60점 이하)
    }
};
