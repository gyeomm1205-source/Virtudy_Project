<template>
  <div class="bg-[#FFC497] flex flex-col items-start px-[0.711rem] py-[0.948rem] rounded-[1.777rem] shadow-[4px_4px_0px_0px_#805143] w-[22.692rem]">
    <div class="flex gap-[0.178rem] items-center px-[0.948rem] py-[0.474rem] w-full">
      <div class="relative shrink-0 size-[1.422rem]">
        <span class="block max-w-none size-full">
           <svg xmlns="http://www.w3.org/2000/svg" width="23" height="23" viewBox="0 0 23 23" fill="none">
              <path d="M8.53457 3.7915C8.53457 3.39887 8.21628 3.08058 7.82365 3.08058C7.43102 3.08058 7.11273 3.39887 7.11273 3.7915H7.82365H8.53457ZM7.11273 6.46061C7.11273 6.85325 7.43102 7.17154 7.82365 7.17154C8.21628 7.17154 8.53457 6.85325 8.53457 6.46061H7.82365H7.11273ZM14.1919 6.46061C14.1919 6.85325 14.5102 7.17154 14.9028 7.17154C15.2955 7.17154 15.6138 6.85325 15.6138 6.46061H14.9028H14.1919ZM15.6138 3.79153C15.6138 3.3989 15.2955 3.08061 14.9028 3.08061C14.5102 3.08061 14.1919 3.3989 14.1919 3.79153H14.9028H15.6138ZM4.28406 16.3031L4.99498 16.3031V16.3031H4.28406ZM4.28407 7.45415L4.99499 7.45415V7.45415H4.28407ZM7.82365 3.7915H7.11273V6.46061H7.82365H8.53457V3.7915H7.82365ZM14.9028 4.79946V5.51038H15.7877V4.79946V4.08854H14.9028V4.79946ZM14.9028 4.79946H14.1919V6.46061H14.9028H15.6138V4.79946H14.9028ZM14.9028 4.79946H15.6138V3.79153H14.9028H14.1919V4.79946H14.9028ZM4.28406 16.3031L3.57314 16.3031C3.57314 18.1619 5.07997 19.6687 6.93875 19.6687V18.9578V18.2469C5.86524 18.2469 4.99498 17.3766 4.99498 16.3031L4.28406 16.3031ZM18.4424 16.3031H17.7315C17.7315 17.3766 16.8613 18.2469 15.7877 18.2469V18.9578V19.6687C17.6465 19.6687 19.1534 18.1619 19.1534 16.3031H18.4424ZM18.4424 7.45415H19.1534C19.1534 5.59538 17.6465 4.08854 15.7877 4.08854V4.79946V5.51038C16.8613 5.51038 17.7315 6.38064 17.7315 7.45415H18.4424ZM4.28407 7.45415H4.99499C4.99499 6.38064 5.86525 5.51038 6.93876 5.51038V4.79946V4.08854C5.07998 4.08854 3.57315 5.59538 3.57315 7.45415H4.28407ZM4.28406 9.32069V10.0316H18.4424V9.32069V8.60977H4.28406V9.32069ZM18.4424 7.45415H17.7315V16.3031H18.4424H19.1534V7.45415H18.4424ZM4.28406 16.3031H4.99498L4.99499 7.45415L4.28407 7.45415L3.57315 7.45415L3.57314 16.3031L4.28406 16.3031ZM6.93876 4.79946V5.51038H14.9028V4.79946V4.08854H6.93876V4.79946ZM15.7877 18.9578V18.2469H6.93875V18.9578V19.6687H15.7877V18.9578Z" fill="#805143"/>
           </svg>
        </span>
      </div>
      <p class="font-['PfStardust30S'] leading-normal text-[#805143] text-[1.25rem] tracking-[-0.05rem]">
        {{ currentMonthName }} {{ currentYear }}
      </p>
    </div>

    <div class="bg-white flex flex-col items-center px-[0.652rem] rounded-[0.948rem] w-full">
      <div class="flex flex-col gap-[0.474rem] items-start w-full">
        
        <div class="flex w-full mt-2">
          <div v-for="day in weekDays" :key="day" 
            class="flex-1 h-[2.431rem] flex items-center justify-center">
            <p class="font-['PfStardust30S'] leading-normal text-[#DFA67B] text-[1.25rem] text-center tracking-[-0.05rem]">
              {{ day }}
            </p>
          </div>
        </div>

        <div v-for="(week, weekIdx) in calendarWeeks" :key="weekIdx" 
          class="flex w-full mb-1">
          <div v-for="(dateObj, dayIdx) in week" :key="dayIdx"
            class="flex-1 h-[2.431rem] cursor-pointer flex items-center justify-center transition-colors duration-200"
            :class="getDayClasses(dateObj, dayIdx, week)"
            @mouseenter="hoveredDate = dateObj.date"
            @mouseleave="hoveredDate = null"
            @click="selectDate(dateObj.date)"
          >
            <p class="font-['PfStardust30S'] leading-normal text-[1.25rem] text-center tracking-[-0.05rem]"
              :class="getDateTextClasses(dateObj)">
              {{ dateObj.date.getDate() }}
            </p>
          </div>
        </div>

      </div>
    </div>

    <div class="flex items-center px-[0.948rem] py-[0.474rem] w-full">
      <div class="flex gap-[0.356rem] items-center">
        <span class="block max-w-none size-full">
          <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 12 12" fill="none">
            <ellipse cx="5.54327" cy="5.83522" rx="5.54327" ry="5.83522" fill="#FFEAAC"/>
          </svg>
        </span>
        <p class="font-['PfStardust30S'] leading-normal text-[#805143] text-[1.25rem] tracking-[-0.05rem] whitespace-nowrap">
          Selected Week
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';

const props = defineProps<{
  selectedDate: Date;
}>();

const emit = defineEmits(['select-date', 'close']);

const viewDate = ref(new Date(props.selectedDate));
const hoveredDate = ref<Date | null>(null);
const weekDays = ['S', 'M', 'T', 'W', 'T', 'F', 'S'];

const currentMonthName = computed(() => {
  const months = ['January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'];
  return months[viewDate.value.getMonth()];
});

const currentYear = computed(() => viewDate.value.getFullYear());

// 주 단위로 달력 데이터 구성 (기존 동일)
const calendarWeeks = computed(() => {
  const year = viewDate.value.getFullYear();
  const month = viewDate.value.getMonth();
  
  const firstDay = new Date(year, month, 1);
  const lastDay = new Date(year, month + 1, 0);
  
  const days = [];
  
  // 지난달 채우기 (1일이 일요일이어도 앞의 6칸을 채움)
  const startPad = firstDay.getDay();
  const padCount = startPad === 0 ? 7 : startPad;
  for (let i = padCount - 1; i >= 0; i--) {
    days.push({ date: new Date(year, month, -i), isCurrentMonth: false });
  }
  
  // 이번달 채우기
  for (let i = 1; i <= lastDay.getDate(); i++) {
    days.push({ date: new Date(year, month, i), isCurrentMonth: true });
  }
  
  // 다음달 채우기
  const remaining = 42 - days.length;
  for (let i = 1; i <= remaining; i++) {
    days.push({ date: new Date(year, month + 1, i), isCurrentMonth: false });
  }
  
  // 주 단위로 그룹화
  const weeks = [];
  for (let i = 0; i < days.length; i += 7) {
    weeks.push(days.slice(i, i + 7));
  }
  
  return weeks;
});

// 같은 주인지 확인
const isSameWeek = (d1: Date, d2: Date) => {
  const getMondayOfWeek = (d: Date) => {
    const day = d.getDay() || 7;
    const monday = new Date(d);
    monday.setHours(0, 0, 0, 0);
    monday.setDate(monday.getDate() - day + 1);
    return monday.getTime();
  };
  return getMondayOfWeek(d1) === getMondayOfWeek(d2);
};

const isHoveredWeek = (date: Date) => {
  return hoveredDate.value && isSameWeek(date, hoveredDate.value);
};

// [중요 수정] dayIdx를 받아 스타일을 분기 처리하는 함수
// 선택된 주의 실제 시작/끝 날짜에만 노란색+둥근 모서리, 중간은 회색, 나머지는 흰색이 되도록 수정
const getDayClasses = (dateObj: { date: Date; isCurrentMonth: boolean }, dayIdx: number, week?: Array<{ date: Date; isCurrentMonth: boolean }>) => {
  const isSelected = isSameWeek(dateObj.date, props.selectedDate);
  const isHovered = isHoveredWeek(dateObj.date);

  // 선택된 주의 시작/끝 날짜 계산 (달이 바뀌어도 전체 달력에서 찾음)
  const allDates = calendarWeeks.value.flat();
  const selectedWeekDates = allDates.filter(d => isSameWeek(d.date, props.selectedDate));
  const weekStartDate = selectedWeekDates[0]?.date;
  const weekEndDate = selectedWeekDates[selectedWeekDates.length - 1]?.date;

  // 호버된 주의 시작/끝 날짜 계산
  let hoveredWeekStartDate: Date | undefined, hoveredWeekEndDate: Date | undefined;
  if (hoveredDate.value) {
    const hoveredWeekDates = allDates.filter(d => isSameWeek(d.date, hoveredDate.value!));
    hoveredWeekStartDate = hoveredWeekDates[0]?.date;
    hoveredWeekEndDate = hoveredWeekDates[hoveredWeekDates.length - 1]?.date;
  }

  let classes = '';

  // 선택된 주 표시(노란색)
  if (isSelected) {
    if (weekStartDate && dateObj.date.getTime() === weekStartDate.getTime()) {
      classes += ' bg-[#FFF2CC] rounded-l-full';
    } else if (
      weekEndDate && dateObj.date.getTime() === weekEndDate.getTime() &&
      isSameWeek(dateObj.date, props.selectedDate)
    ) {
      classes += ' bg-[#FFF2CC] rounded-r-full';
    } else if (
      weekStartDate && weekEndDate &&
      dateObj.date > weekStartDate && dateObj.date < weekEndDate &&
      isSameWeek(dateObj.date, props.selectedDate)
    ) {
      classes += ' bg-[#F3F4F6] rounded-none';
    } else {
      classes += ' bg-white';
    }
  }
  // 호버된 주 표시(회색, 선택된 주가 아닐 때만)
  else if (isHovered) {
    if (hoveredWeekStartDate && dateObj.date.getTime() === hoveredWeekStartDate.getTime()) {
      classes += ' bg-[#F3F4F6] rounded-l-full';
    } else if (
      hoveredWeekEndDate && dateObj.date.getTime() === hoveredWeekEndDate.getTime() &&
      isSameWeek(dateObj.date, hoveredDate.value!)
    ) {
      classes += ' bg-[#F3F4F6] rounded-r-full';
    } else if (
      hoveredWeekStartDate && hoveredWeekEndDate &&
      dateObj.date > hoveredWeekStartDate && dateObj.date < hoveredWeekEndDate &&
      isSameWeek(dateObj.date, hoveredDate.value!)
    ) {
      classes += ' bg-[#F3F4F6] rounded-none';
    } else {
      classes += ' bg-white';
    }
  } else {
    classes += dateObj.isCurrentMonth ? ' bg-white' : ' bg-white opacity-30';
  }

  // 이번 달이 아닌 날짜 투명도 처리
  if (!dateObj.isCurrentMonth) {
    classes += ' opacity-50';
  }

  return classes;
};

const getDateTextClasses = (dateObj: { date: Date; isCurrentMonth: boolean }) => {
  return 'text-[#805143]';
};

const selectDate = (date: Date) => {
  emit('select-date', date);
  emit('close');
};

watch(() => props.selectedDate, (newVal) => {
  viewDate.value = new Date(newVal);
});
</script>