<template>
  <div class="mini-report-container">
    <h3 class="report-title">미니 리포트</h3>
    
    <div class="stats-row">
      <div class="stat-item">
        <span class="value">0h</span> <span class="label">공부시간</span>
      </div>
      <div class="stat-item">
        <span class="value">00%</span> <span class="label">집중도</span>
      </div>
    </div>

    <div class="chart-area">
      <div class="chart-placeholder">
        <div class="pentagon">
          <span>지구력</span>
          <span>집중력</span>
          <span>규칙성</span>
          <span>안정감</span>
          <span>의지력</span>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.mini-report-container {
  background-color: #6B5540; /* 어두운 갈색 배경 */
  border-radius: 12px;
  padding: 20px;
  color: #F0E0C0;
  margin-top: 20px;
}
.report-title {
  color: #D6B48D;
  margin-bottom: 15px;
}
.stats-row {
  display: flex;
  justify-content: space-around;
  background-color: #5A4632;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.value { font-size: 1.5rem; font-weight: bold; }
.label { font-size: 0.8rem; color: #aaa; }

.chart-area {
  background-color: #FFF5E0;
  border-radius: 12px;
  height: 200px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #5A4632;
}
.pentagon {
  position: relative;
  width: 100px;
  height: 100px;
  background: rgba(214, 180, 141, 0.5);
  clip-path: polygon(50% 0%, 100% 38%, 82% 100%, 18% 100%, 0% 38%);
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>