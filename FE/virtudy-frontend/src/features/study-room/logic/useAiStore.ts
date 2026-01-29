import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { AiFocusStatus, AiMouthState } from '@/features/study-room/types/ai.types';

export const useStudyRoomAiStore = defineStore('studyRoomAi', () => {
    // --- State ---
    const concentrationScore = ref<number>(100); // Default to full score
    const focusStatus = ref<AiFocusStatus>('FOCUS');
    const mouthState = ref<AiMouthState>('closed');
    const isBlinking = ref<boolean>(false);

    // --- Getters ---
    const isFocusing = computed(() => focusStatus.value === 'FOCUS');

    const avatarState = computed(() => {
        return {
            isDrowsy: focusStatus.value === 'SLEEP',
            isPhone: focusStatus.value === 'PHONE',
            isAway: focusStatus.value === 'AWAY',
            mouth: mouthState.value,
            blink: isBlinking.value
        };
    });

    // --- Actions ---
    function setConcentrationScore(score: number) {
        concentrationScore.value = Math.min(100, Math.max(0, score));
    }

    function setFocusStatus(status: AiFocusStatus) {
        focusStatus.value = status;
    }

    function setMouthState(state: AiMouthState) {
        mouthState.value = state;
    }

    function triggerBlink() {
        isBlinking.value = true;
        // Auto-reset blink after 100ms (as requested)
        setTimeout(() => {
            isBlinking.value = false;
        }, 150);
    }

    function setBlink(state: boolean) {
        isBlinking.value = state;
    }

    function reset() {
        concentrationScore.value = 100;
        focusStatus.value = 'FOCUS';
        mouthState.value = 'closed';
        isBlinking.value = false;
    }

    return {
        // State
        concentrationScore,
        focusStatus,
        mouthState,
        isBlinking,

        // Getters
        isFocusing,
        avatarState,

        // Actions
        setConcentrationScore,
        setFocusStatus,
        setMouthState,
        triggerBlink,
        setBlink,
        reset
    };
});
