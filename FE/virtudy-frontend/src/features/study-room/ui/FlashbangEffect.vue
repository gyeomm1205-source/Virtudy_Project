<!-- 섬광탄 컴포넌트 (FlashbangEffect.vue) -->

<template>
    <div v-if="visible" class="flashbang-overlay">
        <div class="rainbow-flash"></div>
        <div class="alert-msg">
            🚨 WAKE UP! 🚨
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
    z-index: 9999; /* 최상단 */
    pointer-events: none; /* 클릭 통과 */
    display: flex;
    justify-content: center;
    align-items: center;
}

.rainbow-flash {
    position: absolute;
    width: 100%; height: 100%;
    animation: rainbow 0.15s infinite; /* 매우 빠른 깜빡임 */
    opacity: 0.7;
}

.alert-msg {
    position: relative;
    font-size: 5rem;
    font-weight: 900;
    color: white;
    text-shadow: 4px 4px 0 #000;
    animation: shake 0.5s infinite;
    z-index: 10000;
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
    25% { transform: translate(-5px, 5px) rotate(-5deg); }
    50% { transform: translate(5px, -5px) rotate(5deg); }
    75% { transform: translate(-5px, -5px) rotate(-5deg); }
    100% { transform: translate(0,0) rotate(0deg); }
}
</style>