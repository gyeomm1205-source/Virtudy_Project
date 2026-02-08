<<<<<<< HEAD
=======
<!-- 섬광탄 컴포넌트 (FlashbangEffect.vue) -->

>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
<template>
    <div v-if="visible" class="flashbang-overlay">
        <div class="rainbow-flash"></div>
        <div class="alert-msg">
<<<<<<< HEAD
            WAKE UP!
=======
            🚨 WAKE UP! 🚨
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
        </div>
    </div>
</template>

<script setup lang="ts">
defineProps<{ visible: boolean }>();
</script>

<style scoped>
.flashbang-overlay {
    position: fixed;
    top: 0; left: 0; width: 100vw; height: 100vh;
<<<<<<< HEAD
    z-index: 9999;
    pointer-events: none;
    display: flex;
    justify-content: center;
    align-items: center;
    overflow: hidden; /* 진동 애니메이션 시 스크롤 방지 */
=======
    z-index: 9999; /* 최상단 */
    pointer-events: none; /* 클릭 통과 */
    display: flex;
    justify-content: center;
    align-items: center;
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
}

.rainbow-flash {
    position: absolute;
    width: 100%; height: 100%;
<<<<<<< HEAD
    animation: rainbow 0.1s infinite;
=======
    animation: rainbow 0.15s infinite; /* 매우 빠른 깜빡임 */
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
    opacity: 0.7;
}

.alert-msg {
    position: relative;
<<<<<<< HEAD
    /* 반응형 폰트 사이즈 적용 */
    /* 최소 1.5rem, 화면 너비의 15%, 최대 7rem 사이에서 자동 조절 */
    font-size: clamp(1.5rem, 15vw, 7rem); 
    
    /* 좁은 화면에서 줄바꿈 방지 */
    white-space: nowrap;
    
    /* 기존 스타일 유지 */
=======
    font-size: 5rem;
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
    font-weight: 900;
    color: white;
    text-shadow: 4px 4px 0 #000;
    animation: shake 0.5s infinite;
    z-index: 10000;
<<<<<<< HEAD
    
    /* 텍스트가 컨테이너를 넘지 않도록 안전장치 */
    max-width: 100%;
    text-align: center;
=======
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
}

@keyframes rainbow {
    0% { background: rgba(255,0,0,0.5); }
    20% { background: rgba(255,255,0,0.5); }
    40% { background: rgba(0,255,0,0.5); }
    60% { background: rgba(0,0,255,0.5); }
    80% { background: rgba(255,0,255,0.5); }
    100% { background: rgba(255,0,0,0.5); }
}

@keyframes shake {
    0% { transform: translate(0,0) rotate(0deg); }
<<<<<<< HEAD
    25% { transform: translate(-2px, 2px) rotate(-2deg); } /* 진동 폭 축소 고려 가능 */
    50% { transform: translate(2px, -2px) rotate(2deg); }
    75% { transform: translate(-2px, -2px) rotate(-2deg); }
=======
    25% { transform: translate(-5px, 5px) rotate(-5deg); }
    50% { transform: translate(5px, -5px) rotate(5deg); }
    75% { transform: translate(-5px, -5px) rotate(-5deg); }
>>>>>>> 534ac489f4d1ef6e5778811614806bbc06aaa186
    100% { transform: translate(0,0) rotate(0deg); }
}
</style>