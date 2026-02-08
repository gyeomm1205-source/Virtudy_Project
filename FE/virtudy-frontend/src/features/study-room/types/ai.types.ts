
// AI Focus Status Types
export type AiFocusStatus = 'FOCUS' | 'SLEEP' | 'PHONE' | 'AWAY';

// AI Mouth State Types
export type AiMouthState = 'closed' | 'slightly_open' | 'wide_open';

// Combined AI State Interface
export interface AiStatus {
    concentrationScore: number; // 0-100
    focusStatus: AiFocusStatus;
    mouthState: AiMouthState;
    isBlinking: boolean;
}

// Payload structure for Data Messages received from LiveKit
export interface AiDataPayload {
    category: 'SCORE' | 'STATUS' | 'MOUTH' | 'BLINK' | 'FOCUS' | 'SLEEP' | 'PHONE' | 'AWAY';
    value: string | number | boolean;
}
