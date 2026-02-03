<!-- 섬광탄 타겟 선택 모달 -->

<template>
    <div v-if="isOpen" class="modal-backdrop" @click.self="$emit('close')">
        <div class="modal-box">
            <h3 class="title">⚡ 누구를 깨울까요?</h3>
            
            <ul class="user-list">
                <li v-for="user in targets" :key="user.id">
                    <button class="user-btn" @click="$emit('send', user.id)">
                        <span>{{ user.name }}</span>
                        <span class="score-badge">⚠️ {{ Math.round(user.score) }}점</span>
                    </button>
                </li>
            </ul>

            <button class="cancel-btn" @click="$emit('close')">취소</button>
        </div>
    </div>
</template>

<script setup lang="ts">
defineProps<{
    isOpen: boolean;
    targets: { id: string; name: string; score: number }[];
}>();

defineEmits(['close', 'send']);
</script>

<style scoped>
.modal-backdrop {
    position: fixed;
    top: 0; left: 0; width: 100%; height: 100%;
    background: rgba(0,0,0,0.6);
    display: flex; justify-content: center; align-items: center;
    z-index: 2000;
}
.modal-box {
    background: #FFF8E5;
    border: 4px solid #805143;
    box-shadow: 8px 8px 0 #805143;
    padding: 24px;
    width: 320px;
    text-align: center;
}
.title {
    font-family: 'PfStardust30S';
    color: #805143;
    font-size: 1.5rem;
    margin-bottom: 16px;
}
.user-list {
    list-style: none; padding: 0; margin-bottom: 20px;
    max-height: 300px; overflow-y: auto;
}
.user-btn {
    width: 100%;
    display: flex; justify-content: space-between; align-items: center;
    background: #FFD966;
    border: 2px solid #805143;
    padding: 12px;
    margin-bottom: 8px;
    cursor: pointer;
    font-family: 'PfStardust30S';
    color: #805143;
    font-size: 1.1rem;
    transition: transform 0.1s;
}
.user-btn:hover { transform: scale(1.02); background: #FFC107; }
.score-badge { font-weight: bold; color: #D32F2F; }

.cancel-btn {
    background: #E0E0E0;
    border: 2px solid #805143;
    padding: 8px 20px;
    cursor: pointer;
    font-family: 'PfStardust30S';
    color: #555;
}
</style>