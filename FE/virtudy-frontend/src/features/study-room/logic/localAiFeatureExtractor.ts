import type { FeatureExtractor } from './localAiEngine';
import { FaceMesh } from '@mediapipe/face_mesh';
import { Hands } from '@mediapipe/hands';
import * as tf from '@tensorflow/tfjs-core';
import '@tensorflow/tfjs-backend-webgl';
import '@tensorflow/tfjs-backend-cpu';
import { loadGraphModel, type GraphModel } from '@tensorflow/tfjs-converter';

const MODEL_URL = import.meta.env.VITE_LOCAL_PHONE_MODEL_URL || '/models/yolo11/model.json';
const INPUT_SIZE = 640;

const MEDIAPIPE_BASE_URL = import.meta.env.VITE_MEDIAPIPE_BASE_URL || '/mediapipe';
const LOCAL_AI_DEBUG =
    import.meta.env.VITE_LOCAL_AI_DEBUG === '1' || import.meta.env.VITE_LOCAL_AI_DEBUG === 'true';

function locateMediapipeFile(packageName: string, file: string) {
    if (file.startsWith('http') || file.startsWith('data:') || file.startsWith('blob:')) {
        return file;
    }
    // Some solutions request face_detection assets while running face_mesh.
    const resolvedPackage = file.startsWith('face_detection_solution') ? 'face_detection' : packageName;
    return `${MEDIAPIPE_BASE_URL}/${resolvedPackage}/${file}`;
}

type Landmark = { x: number; y: number; z?: number };
type LandmarkList = Landmark[] | { landmark: Landmark[] };

type FaceMeshResults = {
    multiFaceLandmarks?: LandmarkList[];
};

type HandsResults = {
    multiHandLandmarks?: LandmarkList[];
};

export async function createLocalFeatureExtractor(): Promise<FeatureExtractor> {
    const extractor = new LocalTfjsFeatureExtractor();
    await extractor.init();
    return extractor;
}

class LocalTfjsFeatureExtractor implements FeatureExtractor {
    private faceMesh: FaceMesh;
    private hands: Hands;
    private model: GraphModel | null = null;
    private canvas: HTMLCanvasElement;
    private ctx: CanvasRenderingContext2D;
    private faceCanvas: HTMLCanvasElement;
    private faceCtx: CanvasRenderingContext2D;
    private processing = false;
    private handsAvailable = true;
    private prevFaceCenter: { x: number; y: number } | null = null;
    private staticFrames = 0;
    private loggedModelShape = false;
    private lastFrameLuma: number | undefined;
    private lastFaceMeshResults: FaceMeshResults | null = null;
    private loggedFaceMeshEmpty = false;

    constructor() {
        this.faceMesh = new FaceMesh({
            locateFile: (file) => locateMediapipeFile('face_mesh', file),
        });
        this.faceMesh.setOptions({
            maxNumFaces: 1,
            refineLandmarks: false,
            minDetectionConfidence: 0.2,
            minTrackingConfidence: 0.2,
            selfieMode: true,
        });
        this.faceMesh.onResults((res) => {
            this.lastFaceMeshResults = res;
        });


        this.hands = new Hands({
            locateFile: (file) => locateMediapipeFile('hands', file),
        });
        this.hands.setOptions({
            maxNumHands: 2,
            modelComplexity: 1,
            minDetectionConfidence: 0.5,
            minTrackingConfidence: 0.5,
        });

        this.canvas = document.createElement('canvas');
        const ctx = this.canvas.getContext('2d', { willReadFrequently: true });
        if (!ctx) {
            throw new Error('Failed to create canvas context for Local AI');
        }
        this.ctx = ctx;

        this.faceCanvas = document.createElement('canvas');
        const faceCtx = this.faceCanvas.getContext('2d', { willReadFrequently: true });
        if (!faceCtx) {
            throw new Error('Failed to create face canvas context for Local AI');
        }
        this.faceCtx = faceCtx;
    }

    async init() {
        try {
            await tf.setBackend('webgl');
        } catch {
            await tf.setBackend('cpu');
        }
        await tf.ready();
        this.model = await loadGraphModel(MODEL_URL);
    }

    async estimate(video: HTMLVideoElement, opts: { detectPhone: boolean }) {
        if (this.processing) return null;
        this.processing = true;
        try {
            const faceRes = await this.runFaceMesh(video);
            const faceMeshFaces = faceRes.multiFaceLandmarks?.length ?? 0;
            const firstFace = faceRes.multiFaceLandmarks?.[0];
            const faceLandmarks =
                Array.isArray(firstFace) ? firstFace : firstFace?.landmark;
            const faceLandmarksCount = faceLandmarks?.length ?? 0;
            const { faceDetected, ear, pitch } = this.parseFaceMesh(faceRes, video);
            const facePresent = faceDetected;

            let handCenters: Array<{ x: number; y: number }> = [];
            if (opts.detectPhone) {
                const handsRes = await this.runHands(video);
                handCenters = this.parseHands(handsRes, video);
            }

            let phoneConf = 0;
            let phoneBox: [number, number, number, number] | null = null;
            if (opts.detectPhone && this.model) {
                const result = await this.runPhoneModel(video);
                phoneConf = result.phoneConf;
                phoneBox = result.phoneBox;
            }

            let handInteraction = false;
            if (phoneBox && handCenters.length > 0) {
                const [x1, y1, x2, y2] = phoneBox;
                const px = (x1 + x2) / 2;
                const py = (y1 + y2) / 2;
                const thresh = Math.hypot(video.videoWidth, video.videoHeight) * 0.3;
                for (const h of handCenters) {
                    const dist = Math.hypot(px - h.x, py - h.y);
                    if (dist < thresh) {
                        handInteraction = true;
                        break;
                    }
                }
            }

            return {
                faceDetected,
                facePresent,
                ear,
                pitch,
                phoneConf,
                handInteraction,
                frameLuma: this.lastFrameLuma,
                faceMeshFaces,
                faceLandmarksCount,
            };
        } finally {
            this.processing = false;
        }
    }

    private async runFaceMesh(image: HTMLVideoElement): Promise<FaceMeshResults> {
        const srcW = image.videoWidth;
        const srcH = image.videoHeight;
        // Downscale for performance: FaceMesh runs faster at 480px width.
        const scale = Math.min(1, 480 / Math.max(1, srcW));
        const dstW = Math.max(1, Math.round(srcW * scale));
        const dstH = Math.max(1, Math.round(srcH * scale));
        if (this.faceCanvas.width !== dstW || this.faceCanvas.height !== dstH) {
            this.faceCanvas.width = dstW;
            this.faceCanvas.height = dstH;
        }
        this.faceCtx.drawImage(image, 0, 0, srcW, srcH, 0, 0, dstW, dstH);
        if (LOCAL_AI_DEBUG) {
            this.lastFrameLuma = this.computeFrameLuma(this.faceCtx, dstW, dstH);
        }
        try {
            await this.faceMesh.send({ image: this.faceCanvas });
        } catch (error) {
            if (LOCAL_AI_DEBUG) {
                console.warn('[LocalAI] FaceMesh send failed', error);
            }
            return {};
        }

        const res = this.lastFaceMeshResults ?? {};
        if (LOCAL_AI_DEBUG && !this.loggedFaceMeshEmpty) {
            const faces = res.multiFaceLandmarks ?? [];
            const firstFace = faces[0];
            const count = Array.isArray(firstFace) ? firstFace.length : firstFace?.landmark?.length ?? 0;
            if (faces.length > 0 && count === 0) {
                this.loggedFaceMeshEmpty = true;
                console.warn('[LocalAI] FaceMesh landmarks empty', {
                    faces: faces.length,
                    firstFaceType: firstFace ? Object.prototype.toString.call(firstFace) : 'none',
                    firstFaceKeys: firstFace && typeof firstFace === 'object' ? Object.keys(firstFace as object) : [],
                });
            }
        }
        return res;
    }


    private async runHands(image: HTMLVideoElement): Promise<HandsResults> {
        if (!this.handsAvailable) return {};
        return await new Promise((resolve) => {
            let resolved = false;
            this.hands.onResults((res: HandsResults) => {
                if (resolved) return;
                resolved = true;
                resolve(res);
            });
            this.hands.send({ image }).catch((err) => {
                if (!resolved) {
                    resolved = true;
                    resolve({});
                }
                this.handsAvailable = false;
                console.warn('[LocalAI] Hands failed to load. Disabling hand tracking.', err);
            });
        });
    }

    private parseFaceMesh(res: FaceMeshResults, video: HTMLVideoElement) {
        const w = video.videoWidth;
        const h = video.videoHeight;
        let faceDetected = false;
        let ear: number | null = null;
        let pitch: number | null = null;
        let faceCenter: { x: number; y: number } | null = null;
        let ghostFiltered = false;

        const face = res.multiFaceLandmarks?.[0];
        if (face) {
            const lm = Array.isArray(face) ? face : face.landmark;
            // Guard against incomplete landmark sets.
            if (!lm || lm.length <= 454) {
                return { faceDetected: true, ear: null, pitch: null, faceCenter: null, ghostFiltered: false };
            }
            faceDetected = true;
            // Basic sanity check on face size to avoid false positives when user is absent.
            let minX = 1;
            let minY = 1;
            let maxX = 0;
            let maxY = 0;
            for (const p of lm) {
                if (p.x < minX) minX = p.x;
                if (p.y < minY) minY = p.y;
                if (p.x > maxX) maxX = p.x;
                if (p.y > maxY) maxY = p.y;
            }
            const boxW = Math.max(0, maxX - minX) * w;
            const boxH = Math.max(0, maxY - minY) * h;
            const areaRatio = (boxW * boxH) / (w * h);
            if (areaRatio < 0.02 || areaRatio > 0.6) {
                return { faceDetected: false, ear: null, pitch: null, faceCenter: null, ghostFiltered: true };
            }
            const dist = (i: number, j: number) => {
                const dx = (lm[i].x - lm[j].x) * w;
                const dy = (lm[i].y - lm[j].y) * h;
                return Math.hypot(dx, dy);
            };
            const l_v1 = dist(160, 144);
            const l_v2 = dist(158, 153);
            const l_h = dist(33, 133);
            const r_v1 = dist(385, 380);
            const r_v2 = dist(387, 373);
            const r_h = dist(362, 263);
            const earL = l_h > 1e-6 ? (l_v1 + l_v2) / (2 * l_h) : 0;
            const earR = r_h > 1e-6 ? (r_v1 + r_v2) / (2 * r_h) : 0;
            ear = (earL + earR) / 2;

            const noseY = lm[1].y;
            const chinY = lm[152].y;
            const eyeMidY = (lm[33].y + lm[263].y) / 2;
            const denom = chinY - eyeMidY;
            pitch = denom > 1e-6 ? (noseY - eyeMidY) / denom : 0;

            faceCenter = {
                x: ((lm[234].x + lm[454].x) / 2) * w,
                y: ((lm[10].y + lm[152].y) / 2) * h,
            };

            if (ear < 0.02) {
                faceDetected = false;
                ghostFiltered = true;
            }
        }

        if (faceDetected && faceCenter) {
            const isBlinking = ear !== null && ear < 0.2;
            if (this.prevFaceCenter) {
                const dist = Math.hypot(faceCenter.x - this.prevFaceCenter.x, faceCenter.y - this.prevFaceCenter.y);
                if (isBlinking || dist > 1.5) {
                    this.staticFrames = 0;
                } else {
                    this.staticFrames += 1;
                }
            }
            this.prevFaceCenter = faceCenter;
            if (this.staticFrames > 30) {
                faceDetected = false;
                ear = null;
                pitch = null;
                ghostFiltered = true;
            }
        } else {
            this.staticFrames = 0;
            this.prevFaceCenter = null;
        }

        return { faceDetected, ear, pitch, faceCenter, ghostFiltered };
    }

    private parseHands(res: HandsResults, video: HTMLVideoElement) {
        const w = video.videoWidth;
        const h = video.videoHeight;
        const centers: Array<{ x: number; y: number }> = [];
        if (!res.multiHandLandmarks) return centers;
        for (const hand of res.multiHandLandmarks) {
            const lm = Array.isArray(hand) ? hand : hand.landmark;
            if (!lm || lm.length === 0) continue;
            const sx = lm.reduce((sum, p) => sum + p.x, 0) / lm.length;
            const sy = lm.reduce((sum, p) => sum + p.y, 0) / lm.length;
            centers.push({ x: sx * w, y: sy * h });
        }
        return centers;
    }

    private computeFrameLuma(ctx: CanvasRenderingContext2D, width: number, height: number) {
        if (width <= 0 || height <= 0) return undefined;
        const sampleX = 8;
        const sampleY = 6;
        const stepX = Math.max(1, Math.floor(width / sampleX));
        const stepY = Math.max(1, Math.floor(height / sampleY));
        const img = ctx.getImageData(0, 0, width, height).data;
        let sum = 0;
        let count = 0;
        for (let y = 0; y < height; y += stepY) {
            for (let x = 0; x < width; x += stepX) {
                const idx = (y * width + x) * 4;
                const r = img[idx];
                const g = img[idx + 1];
                const b = img[idx + 2];
                sum += 0.2126 * r + 0.7152 * g + 0.0722 * b;
                count += 1;
            }
        }
        if (count === 0) return undefined;
        return sum / (count * 255);
    }

    private async runPhoneModel(video: HTMLVideoElement) {
        const input = this.prepareInput(video);
        const model = this.model;
        if (!model) {
            input.tensor.dispose();
            return { phoneConf: 0, phoneBox: null as [number, number, number, number] | null };
        }

        const output = model.execute(input.tensor);
        const tensor = Array.isArray(output) ? output[0] : output;
        const data = tensor.dataSync();
        const shape = tensor.shape;
        tensor.dispose();
        input.tensor.dispose();
        if (Array.isArray(output)) {
            output.forEach((t) => t.dispose());
        }

        let phoneConf = 0;
        let phoneBox: [number, number, number, number] | null = null;

        if (!this.loggedModelShape) {
            this.loggedModelShape = true;
            console.log('[LocalAI] phone model output shape:', shape);
        }

        if (shape.length === 3) {
            const [batch, dim1, dim2] = shape;
            if (batch === 1) {
                if (dim1 >= 5) {
                    const stride = dim2;
                    const attrs = dim1;
                    for (let i = 0; i < dim2; i += 1) {
                        const x = data[i];
                        const y = data[stride + i];
                        const w = data[stride * 2 + i];
                        const h = data[stride * 3 + i];
                        let score = this.scoreFromAttrs(data, attrs, stride, i);
                        if (score > 1 || score < 0) {
                            score = 1 / (1 + Math.exp(-score));
                        }
                        if (score > phoneConf) {
                            phoneConf = score;
                            phoneBox = this.boxFromYolo(x, y, w, h, input);
                        }
                    }
                } else if (dim2 >= 5) {
                    const attrs = dim2;
                    for (let i = 0; i < dim1; i += 1) {
                        const base = i * attrs;
                        const x = data[base];
                        const y = data[base + 1];
                        const w = data[base + 2];
                        const h = data[base + 3];
                        let score = this.scoreFromAttrsRow(data, attrs, base);
                        if (score > 1 || score < 0) {
                            score = 1 / (1 + Math.exp(-score));
                        }
                        if (score > phoneConf) {
                            phoneConf = score;
                            phoneBox = this.boxFromYolo(x, y, w, h, input);
                        }
                    }
                }
            }
        }

        return { phoneConf, phoneBox };
    }

    private prepareInput(video: HTMLVideoElement) {
        const srcW = video.videoWidth;
        const srcH = video.videoHeight;
        const r = Math.min(INPUT_SIZE / srcW, INPUT_SIZE / srcH);
        const newW = Math.round(srcW * r);
        const newH = Math.round(srcH * r);
        const dw = (INPUT_SIZE - newW) / 2;
        const dh = (INPUT_SIZE - newH) / 2;

        this.canvas.width = INPUT_SIZE;
        this.canvas.height = INPUT_SIZE;
        this.ctx.fillStyle = 'black';
        this.ctx.fillRect(0, 0, INPUT_SIZE, INPUT_SIZE);
        this.ctx.drawImage(video, 0, 0, srcW, srcH, dw, dh, newW, newH);

        const tensor = tf.tidy(() => {
            const img = tf.browser.fromPixels(this.canvas);
            const floatImg = img.dtype === 'float32' ? img : tf.cast(img, 'float32');
            const normalized = tf.div(floatImg, 255);
            return tf.expandDims(normalized, 0);
        });

        return {
            tensor,
            scale: r,
            padX: dw,
            padY: dh,
            srcW,
            srcH,
        };
    }

    private boxFromYolo(x: number, y: number, w: number, h: number, input: { scale: number; padX: number; padY: number; srcW: number; srcH: number }) {
        let bx = x - w / 2;
        let by = y - h / 2;
        let bw = w;
        let bh = h;

        bx = (bx - input.padX) / input.scale;
        by = (by - input.padY) / input.scale;
        bw = bw / input.scale;
        bh = bh / input.scale;

        const x1 = Math.max(0, Math.min(input.srcW, bx));
        const y1 = Math.max(0, Math.min(input.srcH, by));
        const x2 = Math.max(0, Math.min(input.srcW, bx + bw));
        const y2 = Math.max(0, Math.min(input.srcH, by + bh));

        return [Math.round(x1), Math.round(y1), Math.round(x2), Math.round(y2)] as [number, number, number, number];
    }

    private scoreFromAttrs(data: Float32Array | Int32Array | Uint8Array, attrs: number, stride: number, index: number) {
        if (attrs === 5) return data[stride * 4 + index];
        if (attrs === 6) {
            const obj = data[stride * 4 + index];
            const cls = data[stride * 5 + index];
            return obj * cls;
        }
        let max = -Infinity;
        for (let a = 4; a < attrs; a += 1) {
            const v = data[stride * a + index];
            if (v > max) max = v;
        }
        return max;
    }

    private scoreFromAttrsRow(data: Float32Array | Int32Array | Uint8Array, attrs: number, base: number) {
        if (attrs === 5) return data[base + 4];
        if (attrs === 6) {
            const obj = data[base + 4];
            const cls = data[base + 5];
            return obj * cls;
        }
        let max = -Infinity;
        for (let a = 4; a < attrs; a += 1) {
            const v = data[base + a];
            if (v > max) max = v;
        }
        return max;
    }
}
