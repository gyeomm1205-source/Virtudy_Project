import { ref } from 'vue';
import { RoomManager } from '@/shared/api/livekit/RoomManager';
import { LocalAiEngine, createFeatureExtractor } from './localAiEngine';
import { useStudyRoomAiStore } from './useAiStore';

let engine: LocalAiEngine | null = null;
let engineInitPromise: Promise<void> | null = null;

export function useLocalAiRunner() {
    const isRunning = ref(false);

    const ensureEngine = async () => {
        if (engine) return;
        if (engineInitPromise) {
            await engineInitPromise;
            return;
        }
        engineInitPromise = (async () => {
            const extractor = await createFeatureExtractor();
            const roomManager = RoomManager.getInstance();
            const aiStore = useStudyRoomAiStore();
            const initialScore =
                typeof aiStore.concentrationScore === 'number'
                    ? aiStore.concentrationScore
                    : aiStore.concentrationScore.value;
            engine = new LocalAiEngine(extractor, (eventType, value) => {
                roomManager.emitLocalMessage({
                    type: 'AI_EVENT',
                    data: {
                        eventType,
                        value,
                    },
                });
            }, initialScore);
        })();
        await engineInitPromise;
        engineInitPromise = null;
    };

    const startLocalAi = async (videoEl: HTMLVideoElement | null) => {
        if (!videoEl) return;
        await ensureEngine();
        if (!engine) return;
        const aiStore = useStudyRoomAiStore();
        const initialScore =
            typeof aiStore.concentrationScore === 'number'
                ? aiStore.concentrationScore
                : aiStore.concentrationScore.value;
        await engine.start(videoEl, initialScore);
        isRunning.value = true;
    };

    const stopLocalAi = () => {
        if (engine) {
            engine.stop();
        }
        engine = null;
        engineInitPromise = null;
        isRunning.value = false;
    };

    return {
        startLocalAi,
        stopLocalAi,
        isRunning,
    };
}
