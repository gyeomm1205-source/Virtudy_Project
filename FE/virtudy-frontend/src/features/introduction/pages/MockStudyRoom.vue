<script setup lang="ts">
import { computed } from 'vue';
import CharacterAvatar from '@/shared/ui/avatar/CharacterAvatar.vue';
import RoomBackgroundFrame from '@/features/study-room/ui/RoomBackgroundFrame.vue';
import StudyTimer from '@/shared/ui/StudyTimer.vue';
import FocusTimer from '@/shared/ui/FocusTimer.vue';
import WakeUpModal from '@/features/study-room/ui/WakeUpModal.vue';

// 부모(TutorialPage)에서 제어할 상태들
const props = defineProps<{
  bgState?: 'GREEN' | 'YELLOW' | 'RED'; // 배경 상태
  avatarState?: 'FOCUS' | 'SLEEP' | 'PHONE' | 'AWAY'; // 2번째 캐릭터 상태
  showWakeUpModal?: boolean; // 모달 표시 여부
}>();

// 기본값 설정
const currentBgState = computed(() => props.bgState || 'GREEN');
const currentAvatarState = computed(() => props.avatarState || 'FOCUS');

// 더미 데이터
const roomTitle = "버터 마을 지킴이들";
const roomDescription = "집중 안 하면 마을이 멸망해요!";
const displayName = "나";
const focusSeconds = 1250; 
const totalSeconds = 3600;

const myAvatarConfig = {
    hairFront: 'hair_f_1', hairBack: 'hair_b_1', hairColor: '#7D6F64',
    eyes: 'eyes_1', glasses: 'glasses_1', outfit: 'outfit_1', clothesColor: '#FFFFFF',
};

// 상태별 아바타 Props 변환
const getAvatarProps = (state: string) => ({
    aiDrowsy: state === 'SLEEP' ? 1 : 0,
    aiPhone: state === 'PHONE' ? 1 : 0,
    aiAbsent: state === 'AWAY' ? 1 : 0,
});

// 하트 색상을 점수에 따라 설정하는 함수
const getScoreColor = (score: number) => {
    if (score > 59) return '#B8D576'; // 초록색 (집중)
    if (score > 29) return '#FFD966'; // 노란색 (보통)
    return '#FF6B6B'; // 빨간색 (집중 안함)
};

const getHeartStyle = (score: number) => {
    const base = getScoreColor(score);
    return {
        '--heart-base': base,
        '--heart-line': '#668128', 
        '--heart-shadow': '#668128', 
    };
};

// 더미 점수 데이터
const myScore = 85; // 내 집중 점수
const friendScore = computed(() => {
    // 상태에 따라 점수 변경
    switch(currentAvatarState.value) {
        case 'FOCUS': return 85;
        case 'SLEEP': return 35;
        case 'PHONE': return 25;
        case 'AWAY': return 15;
        default: return 50;
    }
});

const heartStyle = { '--heart-base': '#B8D576', '--heart-line': '#668128', '--heart-shadow': '#668128' };
const bgHeartStyle = { '--heart-base': '#B8D576', '--heart-line': '#668128', '--heart-shadow': '#668128' };
</script>

<template>
<div class="page-container">
    <!-- SVG 심볼 정의 -->
        <svg class="heart-symbols" aria-hidden="true" style="position: absolute; width: 0; height: 0; overflow: hidden;">
            <symbol id="heart-pixel-symbol" viewBox="0 0 32 24">
            <g class="heart-line">
            <path d="M13.3334 2.84446H16V5.51113H13.3334V2.84446Z"/>
            <path d="M10.6667 0.177794H13.3334L13.3334 2.84446H10.6667V0.177794Z"/>
            <path d="M8.00002 0.177794H10.6667V2.84446H8.00002V0.177794Z"/>
            <path d="M5.33335 2.66667H8.00002V5.33333H5.33335V2.66667Z"/>
            <path d="M2.66669 5.51113H5.33335V8.17779H2.66669V5.51113Z"/>
            <path d="M2.66669 8H5.33335V10.6667H2.66669V8Z"/>
            <path d="M5.33335 10.6667H8.00002V13.3333H5.33335V10.6667Z"/>
            <path d="M8.00002 13.3333L10.6667 13.3333V16H8.00002L8.00002 13.3333Z"/>
            <path d="M10.6667 16H13.3334V18.6667H10.6667V16Z"/>
            <path d="M13.3334 18.6667H16V21.3333H13.3334L13.3334 18.6667Z"/>
            <path d="M16 21.3333L18.6667 21.3333V24H16V21.3333Z"/>
            <path d="M18.6667 18.6667H21.3334V21.3333L18.6667 21.3333L18.6667 18.6667Z"/>
            <path d="M21.3334 16H24V18.6667H21.3334L21.3334 16Z"/>
            <path d="M24 13.3333H26.6667V16H24V13.3333Z"/>
            <path d="M26.6667 10.6667H29.3334V13.3333L26.6667 13.3333V10.6667Z"/>
            <path d="M29.3334 8H32V10.6667H29.3334L29.3334 8Z"/>
            <path d="M29.3334 5.51113H32V8.17779H29.3334V5.51113Z"/>
            <path d="M26.6667 2.66667H29.3334V5.33333H26.6667V2.66667Z"/>
            <path d="M24 0H26.6667V2.66667H24V0Z"/>
            <path d="M21.3334 0H24V2.66667H21.3334V0Z"/>
            <path d="M18.6667 2.66667H21.3334L21.3334 5.33333H18.6667V2.66667Z"/>
            <path d="M16 5.51113H18.6667V8.17779H16V5.51113Z"/>
            </g>
            <g class="heart-highlight">
            <path d="M10.6667 5.33337H13.3333V8.00004H10.6667V5.33337Z"/>
            <path d="M8 8.00004L10.6667 8.00004L10.6667 10.6667H8V8.00004Z"/>
            </g>
            <g class="heart-base">
            <path d="M26.6666 2.66663H24V5.33329H26.6666V2.66663Z"/>
            <path d="M10.6666 13.3333H13.3333V16H10.6666V13.3333Z"/>
            <path d="M13.3333 13.3333H16V16H13.3333V13.3333Z"/>
            <path d="M16 13.3333H18.6666V16H16V13.3333Z"/>
            <path d="M16 10.6666H18.6666V13.3333H16V10.6666Z"/>
            <path d="M16 7.99996H18.6666V10.6666H16V7.99996Z"/>
            <path d="M10.6666 2.84442H13.3333V5.51109H10.6666V2.84442Z"/>
            <path d="M7.99998 2.84442H10.6666V5.51109H7.99998V2.84442Z"/>
            <path d="M5.33331 5.33329H7.99998V7.99996H5.33331V5.33329Z"/>
            <path d="M7.99998 5.33329H10.6666V7.99996H7.99998V5.33329Z"/>
            <path d="M13.3333 5.51109H16V8.17775H13.3333L13.3333 5.51109Z"/>
            <path d="M13.3333 7.99996H16V10.6666H13.3333V7.99996Z"/>
            <path d="M13.3333 10.6666H16V13.3333H13.3333L13.3333 10.6666Z"/>
            <path d="M10.6666 10.6666H13.3333L13.3333 13.3333H10.6666V10.6666Z"/>
            <path d="M10.6666 7.99996L13.3333 7.99996V10.6666H10.6666L10.6666 7.99996Z"/>
            <path d="M7.99998 10.6666H10.6666V13.3333L7.99998 13.3333V10.6666Z"/>
            <path d="M5.33331 7.99996H7.99998L7.99998 10.6666H5.33331V7.99996Z"/>
            <path d="M16 18.6666H18.6666V21.3333H16V18.6666Z"/>
            <path d="M18.6666 16H21.3333V18.6666L18.6666 18.6666V16Z"/>
            <path d="M16 16H18.6666V18.6666H16V16Z"/>
            <path d="M13.3333 16H16V18.6666L13.3333 18.6666L13.3333 16Z"/>
            <path d="M21.3333 13.3333H24V16H21.3333L21.3333 13.3333Z"/>
            <path d="M21.3333 10.6666H24V13.3333H21.3333V10.6666Z"/>
            <path d="M21.3333 7.99996H24V10.6666H21.3333V7.99996Z"/>
            <path d="M18.6666 5.33329H21.3333L21.3333 7.99996H18.6666L18.6666 5.33329Z"/>
            <path d="M18.6666 7.99996H21.3333V10.6666H18.6666V7.99996Z"/>
            <path d="M18.6666 10.6666H21.3333V13.3333H18.6666V10.6666Z"/>
            <path d="M18.6666 13.3333H21.3333L21.3333 16H18.6666V13.3333Z"/>
            <path d="M21.3333 5.33329H24L24 7.99996H21.3333L21.3333 5.33329Z"/>
            <path d="M21.3333 2.66663H24V5.33329H21.3333L21.3333 2.66663Z"/>
            <path d="M24 2.66663H26.6666V5.33329H24V2.66663Z"/>
            <path d="M24 5.33329H26.6666V7.99996L24 7.99996L24 5.33329Z"/>
            <path d="M24 7.99996L26.6666 7.99996V10.6666H24V7.99996Z"/>
            <path d="M24 10.6666H26.6666V13.3333L24 13.3333V10.6666Z"/>
            <path d="M26.6666 5.33329H29.3333V7.99996H26.6666V5.33329Z"/>
            <path d="M26.6666 7.99996H29.3333V10.6666H26.6666V7.99996Z"/>
            </g>
            <g id="heart_shadow">
            <path d="M13.3333 21.3333H16V24H13.3333V21.3333Z"/>
            <path d="M10.6667 18.6667H13.3333L13.3333 21.3333L10.6667 21.3333V18.6667Z"/>
            <path d="M16 2.84446H18.6667V5.51113H16V2.84446Z"/>
            <path d="M8 16H10.6667V18.6667H8V16Z"/>
            <path d="M5.33333 13.3333H8V16H5.33333V13.3333Z"/>
            <path d="M2.66667 10.6667H5.33333V13.3333L2.66667 13.3333V10.6667Z"/>
            <path d="M0 8H2.66667V10.6667H0V8Z"/>
            <path d="M0 5.51113H2.66667V8.17779H0V5.51113Z"/>
            <path d="M2.66667 2.84446H5.33333V5.51113H2.66667V2.84446Z"/>
            <path d="M5.33333 0.177794H8V2.84446H5.33333V0.177794Z"/>
            <path d="M18.6667 0H21.3333V2.66667H18.6667V0Z"/>
            </g>
            </symbol>
        </svg>

    <div class="room-layout">
        <div class="content-wrapper">
            <main class="main-window-area">
                
                <div class="room-info-overlay">
                    <h2 class="room-title flex items-center gap-2">
                        {{ roomTitle }}
                    </h2>
                    <p class="room-description">{{ roomDescription }}</p>
                </div>

                <div class="room-controls-overlay">
                    <span class="member-count">2/6명</span>
                    <button class="btn-settings">설정</button>
                    <div class="copy-button-container">
                        <button class="btn-copy-pixel">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none"><path d="M16 20V22H15V23H3V22H2V6H3V5H6V20H16Z" fill="#FFF2CC"/><path d="M22 7V18H21V19H8V18H7V2H8V1H16V7H22Z" fill="#FFF2CC"/></svg>
                        </button>
                    </div>
                    <button class="btn-leave tutorial-target-leave">나가기</button>
                </div>
                
                <RoomBackgroundFrame :bgState="currentBgState" class="tutorial-target-window" />
                
                <div class="timer-floating-widget tutorial-target-timer">
                    <div class="hearts-container">
                        <svg class="heart-svg heart-svg--timer" :style="bgHeartStyle" viewBox="0 0 32 24"><path d="M13.3334 2.84446H16V5.51113H13.3334V2.84446Z" fill="#668128"/></svg>
                        <svg class="heart-svg heart-svg--timer" :style="bgHeartStyle" viewBox="0 0 32 24"><path d="M13.3334 2.84446H16V5.51113H13.3334V2.84446Z" fill="#668128"/></svg>
                        <svg class="heart-svg heart-svg--timer" :style="bgHeartStyle" viewBox="0 0 32 24"><path d="M13.3334 2.84446H16V5.51113H13.3334V2.84446Z" fill="#668128"/></svg>
                    </div>
                    <div class="window-frame">
                        <div class="window-framing">
                            <div class="window-title"><span>집중/전체 타이머</span></div>
                        </div>
                    </div>
                    <div class="timer-display">
                        <div class="focus-timer-area"><FocusTimer :seconds="focusSeconds" /></div>
                        <div class="total-timer-area"><StudyTimer :seconds="totalSeconds"/></div>
                    </div>
                    <div class="pip-btn-area">
                        <button class="btn-pip btn-pixel-action tutorial-target-pip">PIP전환</button>
                        <button class="btn-pixel-action btn-wakeup active tutorial-target-wakeup">깨우기!</button>
                    </div>
                </div>

                <div class="avatar-strip tutorial-target-avatar">
                    <div class="avatar-card local">
                        <div class="avatar-display">
                            <CharacterAvatar :config="myAvatarConfig" :aiDrowsy="0" :aiPhone="0" :aiAbsent="0" />
                        </div>
                        <div class="user-info">
                            <span class="user-name">{{ displayName }}</span>
                            <svg
                                class="heart-svg"
                                :style="getHeartStyle(myScore)"
                                aria-label="my-focus-heart"
                                viewBox="0 0 32 24"
                            >
                                <use href="#heart-pixel-symbol" />
                            </svg>
                        </div>
                    </div>
                    <div class="avatar-card remote tutorial-target-friend">
                        <div class="avatar-display">
                            <CharacterAvatar 
                                :config="myAvatarConfig" 
                                v-bind="getAvatarProps(currentAvatarState)"
                            />
                        </div>
                        <div class="user-info">
                            <span class="user-name">친구</span>
                            <svg
                                class="heart-svg"
                                :style="getHeartStyle(friendScore)"
                                aria-label="friend-focus-heart"
                                viewBox="0 0 32 24"
                            >
                                <use href="#heart-pixel-symbol" />
                            </svg>
                        </div>
                    </div>
                </div>
            </main>

            <button class="btn-side-toggle open">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#805143" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9 18l6-6-6-6"/>
                </svg>
            </button>

            <div class="chat-container-wrapper open">
                <div class="mock-chat-bg w-full h-full bg-[#FFF8E5] border-l-2 border-[#805143] p-4 flex flex-col">
                    <div class="flex-1">
                        <div class="bg-white/50 p-2 rounded mb-2 text-[#805143]">
                            <strong>SYSTEM:</strong> 버터 마을에 오신 것을 환영합니다!
                        </div>
                    </div>
                    <div class="h-12 border-2 border-[#805143] rounded bg-white"></div>
                </div>
            </div>

            <WakeUpModal 
                :isOpen="props.showWakeUpModal || false"
                :targets="['상태변화 친구']"
                @close="() => {}"
                @send="() => {}"
                class="tutorial-target-wakeup-modal"
            />

        </div>
    </div>
</div>
</template>

<style scoped>
/* StudyRoomPage.vue 스타일 복사 및 일부 단순화 */
.page-container { width: 100vw; height: 100vh; background: url('@/assets/room/room_wood_bg.png') center/cover no-repeat; background-color: #dcd0c0; overflow: hidden; }
.room-layout { display: flex; flex-direction: column; height: 100%; }
.content-wrapper { display: flex; flex: 1; height: 100vh; overflow: hidden; position: relative; }
.main-window-area { flex: 3; position: relative; background: url('@/assets/room/room_wood_bg.png') center/cover no-repeat; background-color: #dcd0c0; overflow: hidden; }
.room-info-overlay { position: absolute; top: 20px; left: 20px; z-index: 20; color: white; text-shadow: 1px 1px 2px rgba(0,0,0,0.5); }
.room-title { margin: 0; color: #FFF2CC; text-shadow: 4px 4px 0 #805143; font-family: Ram; font-size: 2.25rem; }
.room-description { margin-left: 10px; font-family: "PfStardust30S"; font-size: 24px; color: #805143; }
.room-controls-overlay { position: absolute; top: 20px; right: 20px; z-index: 20; display: flex; gap: 40px; align-items: center; }
.member-count { color: #805143; font-family: "PfStardust30S"; font-size: 1.5rem; }
.btn-leave { background: #FFD966; border: 2px solid #805143; color: #805143; padding: 12px 32px; font-family: 'PfStardust30S'; font-size: 24px; box-shadow: 4px 4px 0px 0px #805143; cursor: pointer; }
.btn-settings { background: transparent; border: none; font-family: 'PfStardust30S'; font-size: 24px; color: #805143; cursor: pointer; }
.btn-copy-pixel { background: transparent; border: none; width: 24px; height: 24px; cursor: pointer; }
.timer-floating-widget { position: absolute; top: 20%; right: 50px; width: 323px; height: auto; z-index: 10; }
.hearts-container { display: flex; justify-content: flex-end; gap: 5px; margin-bottom: -10px; position: relative; z-index: 11; }
.heart-svg { width: 30px; height: 30px; display: inline-block; }
.window-frame { position: relative; width: 323px; height: 200px; right: -10px; top: 10px; background: #fff8e5; }
.window-framing { position: absolute; width: 100%; height: 100%; border: 2px solid #805143; border-radius: 2px; box-shadow: inset -1px -1px 0 #000, inset 1px 1px 0 #dbdbdb; background: #fff8e5;}
.window-title { position: absolute; height: 30px; left: 3px; right: 3px; top: 3px; background: #805143; display: flex; align-items: center; padding-left: 4px; }
.window-title span { font-family: 'Xcu'; font-size: 23px; color: white; }
.timer-display { position: absolute; width: 100%; top: 55%; left: 47%; transform: translate(-50%, -40%); display: flex; flex-direction: column; align-items: center; z-index: 5; }
.pip-btn-area { position: relative; top: 18px; width: 323px; display: flex; justify-content: flex-end; gap: 8px; }
.btn-pixel-action { background: var(--color-butter2); color: var(--color-choco); border: 2px solid var(--color-choco); border-radius: 50px; padding: 10px; font-family: 'PfStardust30S'; font-size: 1.5rem; box-shadow: 0px 4px 0px var(--color-choco); width: 110px; }
.btn-wakeup.active { background: #FFD966; color: #805143; border-color: #805143; box-shadow: 0px 4px 0px #805143; }
.avatar-strip { position: absolute; bottom: 0; left: 0; width: 100%; height: 50px; background-color:#FFC497; background-image: url('@/assets/room/footer_bar_bg.png'); background-size: cover; display: flex; align-items: center; padding-left: 20px; overflow-y: visible; z-index: 10; }
.avatar-card { position: relative; width: 150px; display: flex; justify-content: center; align-items: center; flex-shrink: 0; }
.avatar-display { position: absolute; bottom: -46px; width: 200px; height: 215px; left: 7px }
.user-info { width: 90%; height: 40px; background: url('@/assets/room/nametag_bg.png') center/100% 100% no-repeat; display: flex; align-items: center; justify-content: center; font-family: 'PfStardust30S'; color: #805143; font-size: 1.7rem; z-index: 11; position: relative; }
.user-name { max-width: 110px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 3px; }

/* 하트 관련 스타일 추가 */
.heart-svg {
  width: 1.5rem;
  height: 1.125rem;
  margin-left: 0.375rem;
  flex-shrink: 0;
}

.heart-line { 
  fill: var(--heart-line); 
}

.heart-base { 
  fill: var(--heart-base); 
}

.heart-shadow { 
  fill: var(--heart-shadow); 
}

.btn-side-toggle { position: absolute; top: 50%; right: 20rem; transform: translateY(-50%); z-index: 50; width: 1.5rem; height: 5rem; display: flex; justify-content: center; align-items: center; border: 1px solid #805143; background: #ffeaac; box-shadow: 4px 4px 0 0 #805143; border-radius: 0.875rem 0 0 0.875rem; }
.chat-container-wrapper { width: 20rem; overflow: hidden; flex-shrink: 0; height: 100%; }
</style>