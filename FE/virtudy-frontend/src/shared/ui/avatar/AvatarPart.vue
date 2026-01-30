// 아바타 파츠 렌더링용 파일입니다

<script setup lang="ts">
import { defineAsyncComponent, computed } from 'vue';

const props = defineProps<{
  category: string; // 예: hair_front
  option: string;   // 예: bangs
  color?: string;   // 예: #FF5733 (변경할 색상, 없으면 원본 유지)
}>();

// 1. SVG 동적 로딩 (Vite 방식)
// assets/avatar/ 폴더에서 [카테고리]_[옵션].svg 파일을 찾음
const SvgComponent = computed(() => {
  // 옵션이 없거나 'none'이면 렌더링 안 함
  if (!props.option || props.option === 'none') return null;

  return defineAsyncComponent({
    // @/assets/avatar/ 경로에서 파일 로드
    loader: () => import(`@/assets/avatar/${props.category}_${props.option}.svg`),
    // errorComponent: { template: '' } // 에러 시 빈 화면 처리
  });
});

// 2. 색상 스타일 (색상이 있을 때만 변수 주입)
const styleObject = computed(() => {
  return props.color ? { '--part-color': props.color } : {};
});
</script>

<template>
  <div class="part-wrapper" :style="styleObject">
    <component :is="SvgComponent" class="svg-content" />
  </div>
</template>

<style scoped>
.part-wrapper {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

/* SVG 내부의 _base 레이어 색상 변경 로직
  ID가 "_base"로 끝나는 요소의 색상을 변경
  SVG가 stroke를 사용하므로 stroke를 변경
*/
:deep([id$="_base"]) {
  /* props.color가 있을 때만 CSS 변수가 적용됨 */
  stroke: var(--part-color, initial) !important;
  
  /* 만약 나중에 면으로 채워진 SVG를 쓴다면 아래 주석을 해제하고 위를 주석 처리 */
  /* fill: var(--part-color, initial) !important; */
}
</style>