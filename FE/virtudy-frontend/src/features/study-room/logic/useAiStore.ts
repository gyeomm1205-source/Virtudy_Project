import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { AiFocusStatus, AiMouthState } from '@/features/study-room/types/ai.types';

export const useStudyRoomAiStore = defineStore('studyRoomAi', () => {
    const SCORE_STORAGE_PREFIX = 'studyRoomAi.score.';
    const currentRoomId = ref<string | null>(null);

    const getSessionStorage = () => {
        if (typeof window === 'undefined') return null;
        return window.sessionStorage;
    };
    const getLocalStorage = () => {
        if (typeof window === 'undefined') return null;
        return window.localStorage;
    };

    const scoreKey = (roomId: string) => `${SCORE_STORAGE_PREFIX}${roomId}`;

    const loadStoredScore = (roomId: string) => {
        const sessionStorage = getSessionStorage();
        const localStorage = getLocalStorage();
        try {
            if (sessionStorage) {
                const raw = sessionStorage.getItem(scoreKey(roomId));
                if (raw) {
                    const parsed = Number(raw);
                    if (Number.isFinite(parsed)) return parsed;
                }
            }
            if (localStorage) {
                const raw = localStorage.getItem(scoreKey(roomId));
                if (raw) {
                    const parsed = Number(raw);
                    if (Number.isFinite(parsed)) return parsed;
                }
            }
        } catch {
            return null;
        }
        return null;
    };

    const saveStoredScore = (roomId: string, score: number) => {
        try {
            getSessionStorage()?.setItem(scoreKey(roomId), String(score));
            getLocalStorage()?.setItem(scoreKey(roomId), String(score));
        } catch {
            // ignore storage errors
        }
    };

    const clearStoredScore = (roomId: string) => {
        try {
            getSessionStorage()?.removeItem(scoreKey(roomId));
            getLocalStorage()?.removeItem(scoreKey(roomId));
        } catch {
            // ignore storage errors
        }
    };

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
        const next = Math.min(100, Math.max(0, score));
        concentrationScore.value = next;
        if (currentRoomId.value) {
            saveStoredScore(currentRoomId.value, next);
        }
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
        if (currentRoomId.value) {
            saveStoredScore(currentRoomId.value, 100);
        }
        focusStatus.value = 'FOCUS';
        mouthState.value = 'closed';
        isBlinking.value = false;
    }

    function setRoomId(roomId: string) {
        currentRoomId.value = roomId;
        const stored = loadStoredScore(roomId);
        if (stored !== null) {
            concentrationScore.value = stored;
        } else {
            concentrationScore.value = 100;
        }
    }

    function clearRoomSession() {
        if (currentRoomId.value) {
            clearStoredScore(currentRoomId.value);
        }
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
        reset,
        setRoomId,
        clearRoomSession
    };
});
