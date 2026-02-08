type FocusState = 'FOCUSED' | 'DROWSY' | 'PHONE' | 'ABSENT' | 'UNKNOWN';
type FocusEventType = 'FOCUS' | 'SLEEP' | 'PHONE' | 'AWAY' | 'SCORE';

type DrowsinessSignal = {
    faceDetected: boolean;
    ear: number | null;
    headPitch: number | null;
    drowsyScore: number;
};

type PhoneSignal = {
    phonePresent: boolean;
    phoneConf: number;
    phoneInUseScore: number;
    phoneInUse: boolean;
};

type AbsenceSignal = {
    faceDetected: boolean;
    absentScore: number;
    absent: boolean;
};

type FrameSignals = {
    drowsy: DrowsinessSignal;
    absent: AbsenceSignal;
    phone: PhoneSignal;
};

type FocusDecision = {
    state: FocusState;
    confidence: number;
    reason: string;
};

type ScoreSnapshot = {
    score: number;
    focusedSec: number;
    drowsySec: number;
    phoneSec: number;
    absentSec: number;
};

type FeatureResult = {
    faceDetected: boolean;
    facePresent: boolean;
    ear: number | null;
    pitch: number | null;
    phoneConf: number;
    handInteraction: boolean;
    frameLuma?: number;
    faceMeshFaces?: number;
    faceLandmarksCount?: number;
};

export type LocalAiEventHandler = (eventType: FocusEventType, value: number) => void;

export interface FeatureExtractor {
    init?: () => Promise<void>;
    estimate: (video: HTMLVideoElement, opts: { detectPhone: boolean }) => Promise<FeatureResult | null>;
    dispose?: () => void;
}

const LOCAL_AI_DEBUG =
    import.meta.env.VITE_LOCAL_AI_DEBUG === '1' || import.meta.env.VITE_LOCAL_AI_DEBUG === 'true';

const LocalAiConfig = {
    ABSENT_WINDOW: 5,
    ABSENT_TRUE_RATIO: 0.6,
    ABSENT_GRACE_SEC: 1.2,

    EAR_DROWSY_TH: 0.2,
    EAR_AWAKE_TH: 0.26,
    PITCH_DROWSY_TH: 0.42,
    DROWSY_WINDOW: 5,
    DROWSY_TRUE_RATIO: 0.6,

    PHONE_CANDIDATE_TH: 0.2,
    PHONE_CONFIRMED_TH: 0.25,
    PHONE_STRONG_TH: 0.45,
    PHONE_USE_WINDOW: 4,
    PHONE_USE_TRUE_RATIO: 0.7,
    PITCH_PHONE_USE_TH: 0.25,

    SCORE_RATE_SCALE: 5.0,
    REWARD_FOCUSED: 1.0,
    PENALTY_FOCUSED: 0.0,
    PENALTY_DROWSY: 3.0,
    PENALTY_PHONE: 2.0,
    PENALTY_ABSENT: 5.0,
    PENALTY_UNKNOWN: 0.5,

    STATUS_SEND_INTERVAL_MS: 100,
    SCORE_SEND_INTERVAL_MS: 1000,
    DROWSY_INTERVAL_MS: 200,
    PHONE_INTERVAL_MS: 2000,
    LOOP_MIN_INTERVAL_MS: 150,
};

class BoolWindow {
    private readonly size: number;
    private readonly trueRatio: number;
    private queue: boolean[] = [];
    private trueCount = 0;

    constructor(size: number, trueRatio: number) {
        this.size = size;
        this.trueRatio = trueRatio;
    }

    update(value: boolean): boolean {
        this.queue.push(value);
        if (value) this.trueCount += 1;
        if (this.queue.length > this.size) {
            const removed = this.queue.shift();
            if (removed) this.trueCount -= 1;
        }
        const ratio = this.queue.length ? this.trueCount / this.queue.length : 0;
        return ratio >= this.trueRatio;
    }

    clear() {
        this.queue = [];
        this.trueCount = 0;
    }
}

class DrowsinessDetector {
    private readonly smoother = new BoolWindow(LocalAiConfig.DROWSY_WINDOW, LocalAiConfig.DROWSY_TRUE_RATIO);
    private eyesClosedStart: number | null = null;
    private readonly minClosedSec = 0.7;

    process(faceDetected: boolean, ear: number | null, headPitch: number | null): DrowsinessSignal {
        if (!faceDetected || ear === null || headPitch === null) {
            return { faceDetected, ear, headPitch, drowsyScore: 0.0 };
        }

        const nowSec = performance.now() / 1000;
        const eyesClosedRaw = ear < LocalAiConfig.EAR_DROWSY_TH;
        if (eyesClosedRaw) {
            if (this.eyesClosedStart === null) this.eyesClosedStart = nowSec;
        } else {
            this.eyesClosedStart = null;
        }
        const closedDuration = this.eyesClosedStart ? nowSec - this.eyesClosedStart : 0;
        const eyesClosed = eyesClosedRaw && closedDuration >= this.minClosedSec;
        const headDropped = headPitch > LocalAiConfig.PITCH_DROWSY_TH;
        let drowsyCandidate = eyesClosed;

        if (headDropped && ear < LocalAiConfig.EAR_DROWSY_TH * 1.05 && closedDuration >= this.minClosedSec) {
            drowsyCandidate = true;
        }

        if (ear >= LocalAiConfig.EAR_AWAKE_TH) {
            this.smoother.clear();
            drowsyCandidate = false;
        }

        const drowsy = this.smoother.update(drowsyCandidate);
        const score = drowsy ? 1.0 : (drowsyCandidate ? 0.6 : 0.0);

        return {
            faceDetected,
            ear,
            headPitch,
            drowsyScore: score,
        };
    }

    reset() {
        this.smoother.clear();
        this.eyesClosedStart = null;
    }
}

class PhoneDetector {
    private state: 'OFF' | 'ON' = 'OFF';
    private fastOnStart: number | null = null;
    private phoneOnlyOnStart: number | null = null;
    private offStart: number | null = null;

    private readonly fastOnHoldSec = 0.08;
    private readonly phoneOnlyOnHoldSec = 0.05;
    private readonly offHoldSec = 0.3;

    process(phoneConf: number, faceDetected: boolean, headPitch: number | null, handInteraction: boolean): PhoneSignal {
        const now = performance.now() / 1000;
        const phoneCandidate = phoneConf >= LocalAiConfig.PHONE_CANDIDATE_TH;
        const phoneConfirmed = phoneConf >= LocalAiConfig.PHONE_CONFIRMED_TH;
        const phoneStrong = phoneConf >= LocalAiConfig.PHONE_STRONG_TH;
        const lookingDown = headPitch !== null && headPitch > LocalAiConfig.PITCH_PHONE_USE_TH;

        const faceMissing = !faceDetected;
        const fastCondition = phoneConfirmed && (handInteraction || lookingDown || faceMissing);
        const occlusionCondition = faceMissing && phoneConfirmed;
        const evidence = phoneStrong || (phoneConfirmed && (handInteraction || faceMissing));

        if (this.state === 'OFF') {
            if (fastCondition || occlusionCondition || evidence) {
                if (this.fastOnStart === null) this.fastOnStart = now;
                if (now - this.fastOnStart >= this.fastOnHoldSec) {
                    this.state = 'ON';
                    this.resetTimers();
                }
            } else {
                this.fastOnStart = null;
            }

            this.phoneOnlyOnStart = null;
        } else {
            if (!phoneCandidate) {
                this.state = 'OFF';
                this.resetTimers();
                return {
                    phonePresent: phoneCandidate,
                    phoneConf,
                    phoneInUseScore: 0.0,
                    phoneInUse: false,
                };
            }

            const offCondition = !phoneCandidate || !(phoneConfirmed || handInteraction || lookingDown);
            if (offCondition) {
                if (this.offStart === null) this.offStart = now;
                if (now - this.offStart >= this.offHoldSec) {
                    this.state = 'OFF';
                    this.resetTimers();
                }
            } else {
                this.offStart = null;
            }
        }

        const phoneInUse = this.state === 'ON';
        return {
            phonePresent: phoneCandidate,
            phoneConf,
            phoneInUseScore: phoneInUse ? 1.0 : 0.0,
            phoneInUse,
        };
    }

    private resetTimers() {
        this.fastOnStart = null;
        this.phoneOnlyOnStart = null;
        this.offStart = null;
    }
}

class AbsenceDetector {
    private readonly smoother = new BoolWindow(LocalAiConfig.ABSENT_WINDOW, LocalAiConfig.ABSENT_TRUE_RATIO);
    private lastFaceTs = performance.now() / 1000;
    private readonly graceSec = LocalAiConfig.ABSENT_GRACE_SEC;

    process(facePresent: boolean, phonePresent: boolean): AbsenceSignal {
        const now = performance.now() / 1000;
        if (facePresent) this.lastFaceTs = now;
        const faceMissing = !facePresent && (now - this.lastFaceTs >= this.graceSec);
        const absentNow = faceMissing && !phonePresent;
        const absent = this.smoother.update(absentNow);
        return {
            faceDetected: facePresent,
            absentScore: absentNow ? 1.0 : 0.0,
            absent,
        };
    }
}

class StateFuser {
    private readonly holdFrames = {
        ABSENT: LocalAiConfig.ABSENT_WINDOW,
        DROWSY: LocalAiConfig.DROWSY_WINDOW,
        PHONE: LocalAiConfig.PHONE_USE_WINDOW,
    } as const;

    private readonly focusRecoverFrames = Math.min(
        LocalAiConfig.ABSENT_WINDOW,
        LocalAiConfig.DROWSY_WINDOW,
        LocalAiConfig.PHONE_USE_WINDOW,
    );

    private tick = 0;
    private lastState: FocusState | null = null;
    private lastChangeTick = 0;
    private lastDecision: FocusDecision | null = null;
    private focusStreak = 0;

    private rawDecide(sig: FrameSignals): FocusDecision {
        if (sig.phone.phoneInUse) {
            return { state: 'PHONE', confidence: sig.phone.phoneInUseScore, reason: 'Phone use detected' };
        }
        if (sig.absent.absent) {
            return { state: 'ABSENT', confidence: 1.0, reason: 'Face missing' };
        }
        if (sig.drowsy.drowsyScore >= 0.9) {
            return { state: 'DROWSY', confidence: sig.drowsy.drowsyScore, reason: 'Eyes closed' };
        }
        if (sig.drowsy.faceDetected) {
            return { state: 'FOCUSED', confidence: 0.8, reason: 'Normal' };
        }
        return { state: 'UNKNOWN', confidence: 0.0, reason: 'Insufficient signals' };
    }

    private switch(decision: FocusDecision): FocusDecision {
        this.lastState = decision.state;
        this.lastDecision = decision;
        this.lastChangeTick = this.tick;
        return decision;
    }

    decide(sig: FrameSignals): FocusDecision {
        this.tick += 1;
        const raw = this.rawDecide(sig);
        const rawState = raw.state;

        if (!this.lastDecision) {
            return this.switch(raw);
        }

        if (rawState === 'UNKNOWN') {
            return this.lastDecision;
        }

        if (rawState === 'FOCUSED') {
            this.focusStreak += 1;
        } else {
            this.focusStreak = 0;
        }

        if (this.lastState === 'FOCUSED') {
            if (rawState === 'FOCUSED') {
                this.lastDecision = raw;
                return raw;
            }
            return this.switch(raw);
        }

        const hold = this.holdFrames[this.lastState as keyof typeof this.holdFrames] ?? 0;
        const held = (this.tick - this.lastChangeTick) < hold;

        if (rawState === 'FOCUSED') {
            if (held || this.focusStreak < this.focusRecoverFrames) {
                return this.lastDecision;
            }
            return this.switch(raw);
        }

        if (rawState === this.lastState) {
            this.lastDecision = raw;
            return this.lastDecision;
        }

        if (held) {
            return this.lastDecision;
        }

        return this.switch(raw);
    }
}

class FocusScorer {
    private score: number;
    private lastT = performance.now() / 1000;
    private focusedSec = 0.0;
    private drowsySec = 0.0;
    private phoneSec = 0.0;
    private absentSec = 0.0;

    constructor(initialScore = 100.0) {
        this.score = Math.min(100, Math.max(0, initialScore));
    }

    update(state: FocusState): ScoreSnapshot {
        const now = performance.now() / 1000;
        let dt = now - this.lastT;
        this.lastT = now;
        if (dt > 1.0) dt = 1.0;
        const rateScale = LocalAiConfig.SCORE_RATE_SCALE;

        if (state === 'FOCUSED') {
            this.focusedSec += dt;
            this.score += LocalAiConfig.REWARD_FOCUSED * dt * rateScale;
        } else if (state === 'DROWSY') {
            this.drowsySec += dt;
        } else if (state === 'PHONE') {
            this.phoneSec += dt;
        } else if (state === 'ABSENT') {
            this.absentSec += dt;
        }

        const penalty = {
            FOCUSED: LocalAiConfig.PENALTY_FOCUSED,
            DROWSY: LocalAiConfig.PENALTY_DROWSY,
            PHONE: LocalAiConfig.PENALTY_PHONE,
            ABSENT: LocalAiConfig.PENALTY_ABSENT,
            UNKNOWN: LocalAiConfig.PENALTY_UNKNOWN,
        }[state] ?? 0.0;

        this.score -= penalty * dt * rateScale;
        this.score = Math.min(100, Math.max(0, this.score));

        return {
            score: this.score,
            focusedSec: this.focusedSec,
            drowsySec: this.drowsySec,
            phoneSec: this.phoneSec,
            absentSec: this.absentSec,
        };
    }
}

class NullFeatureExtractor implements FeatureExtractor {
    private warned = false;
    async init() {
        if (!this.warned) {
            console.warn('[LocalAI] Feature extractor not configured. AI inference is disabled.');
            this.warned = true;
        }
    }
    async estimate(): Promise<FeatureResult | null> {
        return null;
    }
}

export async function createFeatureExtractor(): Promise<FeatureExtractor> {
    try {
        const mod = await import('./localAiFeatureExtractor');
        return await mod.createLocalFeatureExtractor();
    } catch (error) {
        console.warn('[LocalAI] Feature extractor load failed:', error);
        return new NullFeatureExtractor();
    }
}

export class LocalAiEngine {
    private readonly extractor: FeatureExtractor;
    private readonly onEvent: LocalAiEventHandler;
    private drowsyDetector = new DrowsinessDetector();
    private phoneDetector = new PhoneDetector();
    private absenceDetector = new AbsenceDetector();
    private stateFuser = new StateFuser();
    private scorer = new FocusScorer();
    private initialScore = 100;

    private running = false;
    private rafId: number | null = null;
    private lastTickMs = 0;
    private lastStatusSentMs = 0;
    private lastScoreSentMs = 0;
    private lastDrowsyMs = 0;
    private lastPhoneMs = 0;
    private lastValidEvent: FocusEventType = 'FOCUS';
    private lastPhoneSignal: PhoneSignal = {
        phonePresent: false,
        phoneConf: 0,
        phoneInUseScore: 0,
        phoneInUse: false,
    };
    private lastDebugMs = 0;
    private lastFacePresent = false;
    private lastPhoneInUse = false;
    private lastFaceMissingMs: number | null = null;
    private faceRecoveryUntilMs = 0;
    private facePresentStreak = 0;
    private faceMissingStreak = 0;
    private missingGraceUntilMs = 0;

    constructor(extractor: FeatureExtractor, onEvent: LocalAiEventHandler, initialScore = 100) {
        this.extractor = extractor;
        this.onEvent = onEvent;
        this.initialScore = Math.min(100, Math.max(0, initialScore));
        this.scorer = new FocusScorer(this.initialScore);
    }

    async start(video: HTMLVideoElement, initialScore?: number) {
        if (this.running) return;
        if (typeof initialScore === 'number' && Number.isFinite(initialScore)) {
            this.initialScore = Math.min(100, Math.max(0, initialScore));
        }
        this.resetState();
        this.running = true;
        await this.extractor.init?.();
        await this.waitForVideo(video);
        if (!this.running) return;
        this.loop(video);
    }

    stop() {
        this.running = false;
        if (this.rafId !== null) {
            cancelAnimationFrame(this.rafId);
            this.rafId = null;
        }
        this.extractor.dispose?.();
    }

    private async waitForVideo(video: HTMLVideoElement) {
        if (video.readyState >= 2 && video.videoWidth > 0) return;
        await new Promise<void>((resolve) => {
            const onReady = () => {
                video.removeEventListener('loadeddata', onReady);
                resolve();
            };
            video.addEventListener('loadeddata', onReady, { once: true });
        });
    }

    private loop(video: HTMLVideoElement) {
        const tick = async (nowMs: number) => {
            if (!this.running) return;
            if (nowMs - this.lastTickMs < LocalAiConfig.LOOP_MIN_INTERVAL_MS) {
                this.rafId = requestAnimationFrame(tick);
                return;
            }
            this.lastTickMs = nowMs;

            if (video.readyState < 2 || video.videoWidth === 0) {
                this.rafId = requestAnimationFrame(tick);
                return;
            }

            const doDrowsy = (nowMs - this.lastDrowsyMs) >= LocalAiConfig.DROWSY_INTERVAL_MS;
            const doPhone = (nowMs - this.lastPhoneMs) >= LocalAiConfig.PHONE_INTERVAL_MS;
            if (!doDrowsy && !doPhone) {
                this.rafId = requestAnimationFrame(tick);
                return;
            }
            if (doDrowsy) this.lastDrowsyMs = nowMs;
            if (doPhone) this.lastPhoneMs = nowMs;

            const features = await this.extractor.estimate(video, { detectPhone: doPhone });
            if (!features) {
                this.rafId = requestAnimationFrame(tick);
                return;
            }

            const sigPhone = doPhone
                ? this.phoneDetector.process(features.phoneConf, features.faceDetected, features.pitch, features.handInteraction)
                : this.lastPhoneSignal;
            if (doPhone) this.lastPhoneSignal = sigPhone;
            if (features.facePresent) {
                this.facePresentStreak += 1;
                this.faceMissingStreak = 0;
            } else {
                this.faceMissingStreak += 1;
                this.facePresentStreak = 0;
            }
            const facePresentStable = this.facePresentStreak >= 2;
            const faceMissingStable = this.faceMissingStreak >= 3;
            if (this.faceMissingStreak === 1) {
                this.missingGraceUntilMs = nowMs + LocalAiConfig.ABSENT_GRACE_SEC * 1000;
            }
            if (facePresentStable) {
                this.missingGraceUntilMs = 0;
            }

            if (faceMissingStable) {
                if (this.lastFaceMissingMs === null) this.lastFaceMissingMs = nowMs;
            } else if (facePresentStable && this.lastFaceMissingMs !== null) {
                if (nowMs - this.lastFaceMissingMs >= 500) {
                    this.faceRecoveryUntilMs = nowMs + 1200;
                }
                this.lastFaceMissingMs = null;
            }

            const inFaceRecovery = nowMs < this.faceRecoveryUntilMs;
            const inMissingGrace = nowMs < this.missingGraceUntilMs;
            const inUnstableFace = !facePresentStable && !faceMissingStable;
            if (
                inFaceRecovery ||
                faceMissingStable ||
                (!this.lastFacePresent && facePresentStable) ||
                (this.lastPhoneInUse && !sigPhone.phoneInUse)
            ) {
                this.drowsyDetector.reset();
            }

            this.lastFacePresent = facePresentStable;
            this.lastPhoneInUse = sigPhone.phoneInUse;

            const faceDetectedForDrowsy = inFaceRecovery || !facePresentStable ? false : features.faceDetected;
            const sigDrowsy = this.drowsyDetector.process(
                faceDetectedForDrowsy,
                inFaceRecovery ? null : features.ear,
                inFaceRecovery ? null : features.pitch,
            );
            const phonePresentStrong = sigPhone.phoneInUse || sigPhone.phoneConf >= LocalAiConfig.PHONE_CONFIRMED_TH;
            let sigAbsent = this.absenceDetector.process(facePresentStable, phonePresentStrong);
            if (inUnstableFace) {
                sigAbsent = { ...sigAbsent, absent: false, absentScore: 0.0 };
            }
            let signals: FrameSignals = { drowsy: sigDrowsy, absent: sigAbsent, phone: sigPhone };
            if ((inMissingGrace || inUnstableFace) && !sigAbsent.absent) {
                signals = {
                    drowsy: { ...sigDrowsy, faceDetected: false, drowsyScore: 0.0 },
                    absent: sigAbsent,
                    phone: { ...sigPhone, phoneInUse: false, phoneInUseScore: 0.0 },
                };
            }
            const decision = this.stateFuser.decide(signals);

            const rawState = decision.state;
            let eventType: FocusEventType;
            if (rawState !== 'UNKNOWN') {
                if (rawState === 'FOCUSED') eventType = 'FOCUS';
                else if (rawState === 'DROWSY') eventType = 'SLEEP';
                else if (rawState === 'ABSENT') eventType = 'AWAY';
                else eventType = 'PHONE';
                this.lastValidEvent = eventType;
            } else {
                eventType = this.lastValidEvent;
            }

            const value = eventType === 'FOCUS' ? 0 : 1;

            const stateForScore =
                rawState !== 'UNKNOWN'
                    ? rawState
                    : eventType === 'FOCUS'
                        ? 'FOCUSED'
                        : eventType === 'SLEEP'
                            ? 'DROWSY'
                            : eventType === 'AWAY'
                                ? 'ABSENT'
                                : 'PHONE';
            const snap = this.scorer.update(stateForScore);

            if (LOCAL_AI_DEBUG && nowMs - this.lastDebugMs >= 2000) {
                this.lastDebugMs = nowMs;
                const payload = {
                    video: {
                        readyState: video.readyState,
                        width: video.videoWidth,
                        height: video.videoHeight,
                        paused: video.paused,
                        currentTime: video.currentTime,
                    },
                    faceDetected: features.faceDetected,
                    facePresent: features.facePresent,
                    ear: features.ear,
                    pitch: features.pitch,
                    phoneConf: features.phoneConf,
                    handInteraction: features.handInteraction,
                    frameLuma: features.frameLuma,
                    faceMeshFaces: features.faceMeshFaces,
                    faceLandmarksCount: features.faceLandmarksCount,
                    drowsyScore: sigDrowsy.drowsyScore,
                    drowsyFace: sigDrowsy.faceDetected,
                    absent: sigAbsent.absent,
                    absentScore: sigAbsent.absentScore,
                    phoneInUse: sigPhone.phoneInUse,
                    phonePresent: sigPhone.phonePresent,
                    phoneSignalConf: sigPhone.phoneConf,
                    decision: decision.state,
                    confidence: decision.confidence,
                    reason: decision.reason,
                    eventType,
                    score: snap.score,
                };
                console.log('[LocalAI]', JSON.stringify(payload));
            }

            if (nowMs - this.lastStatusSentMs >= LocalAiConfig.STATUS_SEND_INTERVAL_MS) {
                this.onEvent(eventType, value);
                this.lastStatusSentMs = nowMs;
            }

            if (nowMs - this.lastScoreSentMs >= LocalAiConfig.SCORE_SEND_INTERVAL_MS) {
                this.onEvent('SCORE', Math.round(snap.score));
                this.lastScoreSentMs = nowMs;
            }

            this.rafId = requestAnimationFrame(tick);
        };

        this.rafId = requestAnimationFrame(tick);
    }

    private resetState() {
        this.drowsyDetector = new DrowsinessDetector();
        this.phoneDetector = new PhoneDetector();
        this.absenceDetector = new AbsenceDetector();
        this.stateFuser = new StateFuser();
        this.scorer = new FocusScorer(this.initialScore);
        this.lastTickMs = 0;
        this.lastStatusSentMs = 0;
        this.lastScoreSentMs = 0;
        this.lastDrowsyMs = 0;
        this.lastPhoneMs = 0;
        this.lastValidEvent = 'FOCUS';
        this.lastPhoneSignal = {
            phonePresent: false,
            phoneConf: 0,
            phoneInUseScore: 0,
            phoneInUse: false,
        };
    }
}
