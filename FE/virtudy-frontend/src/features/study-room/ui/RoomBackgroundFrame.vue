<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted, watch, nextTick } from 'vue';
import bgSky from '@/assets/bg_sky_1.png';
import bgCloud from '@/assets/bg_cloud.png';
import bgVillage from '@/assets/bg_village.png';
import bgPhoto2 from '@/assets/room/bg_photo_2.png';
import bgPhoto3 from '@/assets/room/bg_photo_3.png';
import frameBorder from '@/assets/room/frame_border.png';
import meteorBig from '@/assets/room/meteor_big.png';
import meteorSmall from '@/assets/room/meteor_small.png';

// 상태 타입 정의
type BgState = 'GREEN' | 'YELLOW' | 'RED';

const props = defineProps<{
    bgState: BgState;
}>();

// 상태에 따른 배경 이미지 선택
// YELLOW, RED 상태일 때만 사용할 단일 이미지 소스
const singleBgSrc = computed(() => {
    switch (props.bgState) {
    case 'YELLOW': return bgPhoto2;
    case 'RED': return bgPhoto3;
    default: return null; // GREEN일 때는 이 값을 안 씀
    }
});

// ==========================================
// 🖱️ 마우스 패럴랙스 로직 (GREEN 전용)
// ==========================================
const mouseX = ref(0);
const mouseY = ref(0);

const handleMouseMove = (e: MouseEvent) => {
    if (props.bgState !== 'GREEN') return;

    const { innerWidth, innerHeight } = window;
  // 화면 중앙을 0,0으로 하는 좌표계 (-1 ~ 1)
  mouseX.value = (e.clientX / innerWidth) * 2 - 1;
  mouseY.value = (e.clientY / innerHeight) * 2 - 1;
};

// 1. 하늘 (Sky): 움직이지 않음 (고정)

// 구름 (Cloud): 천천히 움직임 (뒤에 있는 느낌)
const cloudStyle = computed(() => {
    if (props.bgState !== 'GREEN') return {};
  const moveX = mouseX.value * 8;  // 좌우 8px
  const moveY = mouseY.value * 4;  // 상하 4px

    return {
    transform: `scale(1.1) translate(${-moveX}px, ${-moveY}px)`,
    transition: 'transform 0.1s ease-out'
    };
});

// 마을 (Village): 많이 움직임 (앞에 있는 느낌)
const villageStyle = computed(() => {
    if (props.bgState !== 'GREEN') return {};
        const moveX = mouseX.value * 20; // 좌우 20px (구름보다 큼)
        const moveY = mouseY.value * 10; // 상하 10px

    return {
        transform: `scale(1.1) translate(${-moveX}px, ${-moveY}px)`,
        transition: 'transform 0.1s ease-out'
    };
});


// ==========================================
// 🔥 불꽃 효과 로직
// ==========================================
const smokeCanvasRef = ref<HTMLCanvasElement | null>(null);
let animationId: number | null = null;
let particles: Particle[] = [];
let ctx: CanvasRenderingContext2D | null = null;

// 유틸리티 함수
const between = (min: number, max: number) => Math.random() * (max - min) + min;

// 파티클 클래스 정의
class Particle {
    x: number = 0;
    y: number = 0;
    vx: number = 0;
    vy: number = 0;
    size: number = 0;
    color: string = '';
    life: number = 0;
    maxLife: number = 0; // 전체 수명 기억용
    g: number = 0; // 중력 가속도

    constructor(w: number, h: number) {
        this.reset(w, h);
    }

    reset(w: number, h: number) {
        // 가로 위치: 0부터 화면 끝(w)까지 전체 영역에서 랜덤 생성
        this.x = Math.random() * w; 
        
        // 세로 위치: 화면 맨 아래(h)에서 시작 (약간의 랜덤 오차 추가)
        this.y = h + Math.random() * 20; 
        
        this.size = Math.random() * 1 + 1; // 크기를 조금 더 다양하게 (1~2)

        // 속도: 천천히 올라가도록 설정
        this.vx = Math.random() * 0.6 - 0.3; // 좌우로 살짝만 흔들림
        this.vy = between(-0.3, -0.8); // 위로 천천히 올라가는 속도 (숫자가 작을수록 느림)

        // 가속도 설정 (음수 = 위쪽으로 가속)
        this.g = -0.015;
        
        // 수명: 화면 높이의 40% ~ 80% 사이 거리만큼만 이동하고 사라짐
        this.maxLife = Math.abs((h * (Math.random() * 0.4 + 0.4)) / this.vy);
        this.life = this.maxLife;

        const colors = ['#e74c3c', '#FDE9AC', '#F79021', '#F9D181']; // 붉은 계열
        this.color = colors[Math.floor(Math.random() * colors.length)];
    }

    update() {
        this.x += this.vx;
        this.y += this.vy; 
        // 가속도(g) 제거: 등속도로 차분하게 올라가게 함
        
        this.life--;
        
        // 수명이 20% 남았을 때 (화면 상단에 거의 다 왔을 때) 검은색 연기로 변함
        if (this.life < this.maxLife * 0.2) {
            this.color = 'rgba(45, 16, 41, 0.5)'; // 반투명 검은색
        }
    }

    draw(context: CanvasRenderingContext2D) {
        context.beginPath();
        context.fillStyle = this.color;
        context.arc(this.x, this.y, this.size, 0, Math.PI * 2, false);
        context.fill();
    }
}

const initParticles = () => {
    if (!smokeCanvasRef.value) return;
    const canvas = smokeCanvasRef.value;
    particles = [];
    
    // 파티클 개수 40개
    for (let i = 0; i < 40; i++) {
        const p = new Particle(canvas.width, canvas.height);
        // 초기에 화면 중간에도 파티클이 이미 차 있도록 y값을 랜덤 분산 (초기 로딩 시 자연스럽게)
        p.y = Math.random() * canvas.height;
        // y 위치에 맞춰 life도 미리 깎아둠 (자연스러운 시작)
        p.life = p.maxLife * (p.y / canvas.height);
        particles.push(p);
    }
};
const animate = () => {
    if (!smokeCanvasRef.value || !ctx) return;
    const canvas = smokeCanvasRef.value;

    ctx.clearRect(0, 0, canvas.width, canvas.height);

    for (let i = 0; i < particles.length; i++) {
        const p = particles[i];
        p.draw(ctx);
        p.update();

        if (p.life < 1) {
            p.reset(canvas.width, canvas.height);
        }
    }

    animationId = requestAnimationFrame(animate);
};

const startAnimation = async () => {
    if (animationId) return;
    
    // DOM 업데이트를 확실히 기다림
    await nextTick();
    
    if (!smokeCanvasRef.value) return;
    const canvas = smokeCanvasRef.value;
    
    // 캔버스 크기를 부모 요소에 맞춤
    canvas.width = canvas.offsetWidth;
    canvas.height = canvas.offsetHeight;
    
    ctx = canvas.getContext('2d');
    initParticles();
    animate();
};

const stopAnimation = () => {
    if (animationId) {
        cancelAnimationFrame(animationId);
        animationId = null;
    }
    // 멈출 때 캔버스 깨끗하게 지우기
    if (ctx && smokeCanvasRef.value) {
        ctx.clearRect(0, 0, smokeCanvasRef.value.width, smokeCanvasRef.value.height);
    }
};

// immediate: true 제거 (Mounted 시점에 처리하기 위함)
watch(() => props.bgState, (newState) => {
    if (newState === 'YELLOW') {
        startAnimation();
    } else {
        stopAnimation();
    }
});

onMounted(() => {
    // 마우스 이벤트 등록
    window.addEventListener('mousemove', handleMouseMove);

    // 컴포넌트가 마운트된 후, 초기 상태가 YELLOW라면 애니메이션 시작
    if (props.bgState === 'YELLOW') {
        startAnimation();
    }

    window.addEventListener('resize', () => {
        if (props.bgState === 'YELLOW' && smokeCanvasRef.value) {
            smokeCanvasRef.value.width = smokeCanvasRef.value.offsetWidth;
            smokeCanvasRef.value.height = smokeCanvasRef.value.offsetHeight;
        }
    });
});

onUnmounted(() => {
    window.removeEventListener('mousemove', handleMouseMove);
    stopAnimation();
});
</script>

<template>
<div class="frame-container">

    <template v-if="bgState === 'GREEN'">
        <img :src="bgSky" class="scene-photo layer-sky" key="bg-sky" />
        
        <img 
        :src="bgCloud" 
        class="scene-photo layer-cloud" 
        :style="cloudStyle"
        key="bg-cloud" 
        />
        
        <img 
        :src="bgVillage" 
        class="scene-photo layer-village" 
        :style="villageStyle"
        key="bg-village" 
        />
    </template>

    <img 
        v-else
        :src="singleBgSrc || ''" 
        alt="Background" 
        class="scene-photo" 
        :class="{ 'shake-effect': bgState === 'YELLOW' }"
        key="bg-single" 
    />

    <div v-if="bgState === 'YELLOW'" class="effect-layer meteor-layer shake-effect" key="meteor-layer">
        <img :src="meteorBig" class="meteor big-1" alt="" />
        <img :src="meteorBig" class="meteor big-2" alt="" />
        <img :src="meteorSmall" class="meteor small-1" alt="" />
        <img :src="meteorSmall" class="meteor small-2" alt="" />
    </div>

    <canvas 
        v-if="bgState === 'YELLOW'" 
        ref="smokeCanvasRef" 
        class="smoke-canvas shake-effect"
        key="smoke-canvas"
    ></canvas>

    <div v-if="bgState === 'RED'" class="effect-layer warning-layer" key="warning-layer">
        <div class="warning-bar top"></div>
        <div class="warning-text">WARNING!!</div>
        <div class="warning-bar bottom"></div>
        <div class="red-flash-overlay"></div>
    </div>

    <img :src="frameBorder" alt="Frame" class="scene-frame" key="frame-border" />
</div>
</template>

<style scoped>
/* 반응형 프레임 컨테이너 */
.frame-container {
    position: absolute;
    top: 50%;
    left: 40%; /* 전체 레이아웃 기준 위치 */
    transform: translate(-50%, -50%);
    
    /* 반응형 크기 설정 */
    width: 70%;         /* 화면 너비의 70% 차지 */
    max-width: 900px;  /* 너무 커지지 않게 제한 */
    min-width: 500px;   /* 너무 작아지지 않게 제한 */
    
    /* 비율 유지 (700px : 450px 비율) */
    aspect-ratio: 700 / 450;
    
    display: flex;
    justify-content: center;
    align-items: center;
    
    /* 효과가 프레임 밖으로 나가지 않도록 자름 */
    overflow: hidden; 
}

/* 배경 사진 */
.scene-photo {
    position: absolute;
    top: 3%;   /* 프레임 안쪽으로 배치되도록 미세 조정 */
    left: 3%;
    width: 94%;
    height: 94%;
    object-fit: cover; 
}

/* 레이어별 z-index 설정 (순서대로 쌓임) */
.layer-sky { z-index: 1; }
.layer-cloud { z-index: 2; }
.layer-village { z-index: 3; }

/* 액자 프레임 */
.scene-frame {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 10; /* 가장 위 */
    pointer-events: none;
}

/* --------------------------------------------------------
운석 애니메이션 (YELLOW) 
   -------------------------------------------------------- */
.meteor-layer {
    position: absolute;
    top: 0; left: 0; width: 100%; height: 100%;
    z-index: 5;
    /* 부모(.frame-container)에도 overflow:hidden이 있지만, 
       여기서도 영역을 잡아 bgPhoto2(프레임 내부) 밖에서는 안 보이게 함 */
    overflow: hidden; 
}

.meteor {
    position: absolute;
    opacity: 1; /* 항상 선명하게 */
}

/* 우상단 -> 좌하단 이동 애니메이션 */
@keyframes meteor-fall {
    0% {
        transform: translate(1100px, -900px); /* 시작: 오른쪽 위 멀리 */
    }
    100% {
        transform: translate(-1100px, 900px); /* 끝: 왼쪽 아래 멀리 */
    }
}

/* 개별 운석 설정 */
.big-1 {
    width: 45%;
    right: -10%; top: -10%;
    animation: meteor-fall 0.8s linear infinite;
}

.big-2 {
    width: 25%;
    right: 20%; top: -20%;
    animation: meteor-fall 1.4s linear infinite 0.5s;
}

.small-1 {
    width: 18%;
    right: 50%; top: -30%;
    animation: meteor-fall 1.8s linear infinite 0.2s;
}

.small-2 {
    width: 15%;
    right: 0%; top: -40%;
    animation: meteor-fall 2.2s linear infinite 0.8s;
}


/* --------------------------------------------------------
  경고 애니메이션 (RED)
   -------------------------------------------------------- */
.warning-layer {
    position: absolute;
    top: 0; left: 0; width: 100%; height: 100%;
    z-index: 6;
}

/* 경고 띠 */
.warning-bar {
    position: absolute;
    left: 0;
    width: 100%;
    height: 25%; /* 반응형 높이 */
    z-index: 7;
    
    /* 사선 패턴 */
    background: repeating-linear-gradient(
        -45deg,
        #c7212c,
        #c7212c 30px,
        transparent 30px,
        transparent 60px
    );

    background-size: 85px 85px;
    animation: move-stripes 1s linear infinite;
    box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
}

.warning-bar.top { top: 3%; }     /* 사진 영역에 맞춤 */
.warning-bar.bottom { bottom: 3%; }

/* WARNING 텍스트 */
.warning-text {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    z-index: 8;
    
    font-size: clamp(3rem, 8vw, 9rem); /* 반응형 폰트 크기 */
    font-weight: 800;
    color: #c7212c;
    text-transform: uppercase;
    letter-spacing: 5px;
    font-family: 'PfStardust30S', sans-serif;
    
    /* 외곽선 및 그림자 */
    -webkit-text-stroke: 2px rgb(0, 0, 0); 
    text-shadow: 0 0 10px rgba(0,0,0,0.5);

    animation: flash-text 0.5s step-end infinite;
}

/* 전체 붉은 점멸 효과 */
.red-flash-overlay {
    position: absolute;
    top: 0; left: 0; width: 100%; height: 100%;
    background-color: rgba(255, 0, 0, 0.2);
    z-index: 5;
    animation: flash-overlay 1s ease-in-out infinite alternate;
}

/* 키프레임 정의 */
@keyframes move-stripes {
    0% { background-position: 0 0; }
    100% { background-position: -85px 0; }
}

@keyframes flash-text {
    0%, 100% { opacity: 1; }
    50% { opacity: 0; }
}

@keyframes flash-overlay {
    0% { opacity: 0; }
    100% { opacity: 0.4; }
}

/* 캔버스 스타일 */
.smoke-canvas {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 4; /* 사진(1) 위, 운석(5) 아래. 필요시 z-index 조절 */
    pointer-events: none;
}

/* 지진 효과 (가끔씩 흔들림) */
.shake-effect {
    /* 5초 주기로 반복 (4초 대기 + 1초 흔들림) */
    animation: earthquake 5s infinite; 
    /* 하드웨어 가속을 켜서 끊김 방지 */
    will-change: transform; 
}

@keyframes earthquake {
    /* 0% ~ 90% : 평온한 상태 (가만히 있음) */
    0%, 90% {
        transform: translate(0, 0);
    }
    /* 91% ~ 100% : 지진 발생! (빠르게 진동) */
    91% { transform: translate(-2px, 1px); }
    92% { transform: translate(1px, -2px); }
    93% { transform: translate(-3px, 0px); }
    94% { transform: translate(2px, 3px); }
    95% { transform: translate(0px, -3px); }
    96% { transform: translate(-2px, 2px); }
    97% { transform: translate(3px, -1px); }
    98% { transform: translate(-1px, 3px); }
    99% { transform: translate(2px, -2px); }
    100% { transform: translate(0, 0); }
}
</style>