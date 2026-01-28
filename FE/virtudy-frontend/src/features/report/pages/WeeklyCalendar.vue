<template>
  <div class="absolute top-[60px] right-0 z-50 bg-[#FDF6E3] border-2 border-[var(--color-choco)] rounded-[20px] p-4 w-[300px] shadow-lg">
    <div class="flex justify-between items-center mb-4 px-2">
      <button @click="changeMonth(-1)" class="text-[var(--color-choco)] text-xl">&lt;</button>
      <span class="text-[var(--color-choco)] font-bold font-['Ram']">{{ currentYear }} {{ currentMonthName }}</span>
      <button @click="changeMonth(1)" class="text-[var(--color-choco)] text-xl">&gt;</button>
    </div>
    
    <div class="grid grid-cols-7 gap-1 text-center font-['PfStardust30S']">
      <div v-for="day in weekDays" :key="day" class="text-[var(--color-syrup)] text-sm mb-2">{{ day }}</div>
      
      <div 
        v-for="(dateObj, idx) in calendarDays" 
        :key="idx"
        class="h-8 flex items-center justify-center cursor-pointer rounded-full relative"
        :class="{
          'text-gray-300': !dateObj.isCurrentMonth,
          'text-[var(--color-choco)]': dateObj.isCurrentMonth,
          'bg-gray-200': isHoveredWeek(dateObj.date) && dateObj.isCurrentMonth, /* 호버 시 주 전체 회색 */
          'bg-[var(--color-butter)] text-white': isSelectedWeek(dateObj.date), /* 선택된 주 강조 */
        }"
        @mouseenter="hoveredDate = dateObj.date"
        @mouseleave="hoveredDate = null"
        @click="selectDate(dateObj.date)"
      >
        <span class="z-10 relative">{{ dateObj.date.getDate() }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';

const props = defineProps<{
  selectedDate: Date; // 현재 선택된 기준 날짜
}>();

const emit = defineEmits(['select-date', 'close']);

const viewDate = ref(new Date(props.selectedDate));
const hoveredDate = ref<Date | null>(null);
const weekDays = ['S', 'M', 'T', 'W', 'T', 'F', 'S'];

// 월 이름
const currentMonthName = computed(() => viewDate.value.toLocaleString('default', { month: 'long' }));
const currentYear = computed(() => viewDate.value.getFullYear());

// 달력 데이터 생성
const calendarDays = computed(() => {
  const year = viewDate.value.getFullYear();
  const month = viewDate.value.getMonth();
  
  const firstDay = new Date(year, month, 1);
  const lastDay = new Date(year, month + 1, 0);
  
  const days = [];
  
  // 지난달 채우기
  const startPad = firstDay.getDay();
  for (let i = startPad - 1; i >= 0; i--) {
    days.push({ date: new Date(year, month, -i), isCurrentMonth: false });
  }
  
  // 이번달 채우기
  for (let i = 1; i <= lastDay.getDate(); i++) {
    days.push({ date: new Date(year, month, i), isCurrentMonth: true });
  }
  
  // 다음달 채우기 (42칸 맞추기 위해)
  const remaining = 42 - days.length;
  for (let i = 1; i <= remaining; i++) {
    days.push({ date: new Date(year, month + 1, i), isCurrentMonth: false });
  }
  
  return days;
});

const changeMonth = (delta: number) => {
  viewDate.value = new Date(viewDate.value.getFullYear(), viewDate.value.getMonth() + delta, 1);
};

// 주간 확인 로직 (같은 주인지 체크)
const isSameWeek = (d1: Date, d2: Date | null) => {
  if (!d2) return false;
  // 월요일~일요일 기준 주차 계산 로직 필요 (간단히 ISO 주차 사용하거나 차이 계산)
  // 여기서는 간단히: 해당 날짜의 월요일을 구해서 비교
  const getMon = (d: Date) => {
    const day = d.getDay() || 7; // 일요일(0)을 7로 취급
    const mon = new Date(d);
    mon.setHours(0,0,0,0);
    mon.setDate(mon.getDate() - day + 1);
    return mon.getTime();
  };
  return getMon(d1) === getMon(d2);
};

const isHoveredWeek = (date: Date) => isSameWeek(date, hoveredDate.value);
const isSelectedWeek = (date: Date) => isSameWeek(date, props.selectedDate);

const selectDate = (date: Date) => {
  emit('select-date', date);
  emit('close');
};

watch(() => props.selectedDate, (newVal) => {
  viewDate.value = new Date(newVal);
});
</script>