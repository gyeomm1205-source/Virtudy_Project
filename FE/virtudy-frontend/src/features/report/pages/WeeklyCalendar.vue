<template>
  <div class="bg-[#FFC497] flex flex-col items-start px-[0.711rem] py-[0.948rem] rounded-[1.777rem] shadow-[4px_4px_0px_0px_#805143] w-[22.692rem]">
    <!-- Date Filter Header -->
    <div class="flex gap-[0.178rem] items-center px-[0.948rem] py-[0.474rem] w-full">
      <div class="relative shrink-0 size-[1.422rem]">
        <img 
          alt="calendar icon" 
          class="block max-w-none size-full" 
          src="/src/assets/icons/calendar.svg" 
        />
      </div>
      <p class="font-['PfStardust30S'] leading-normal text-[#805143] text-[1.25rem] tracking-[-0.05rem]">
        {{ currentMonthName }} {{ currentYear }}
      </p>
    </div>

    <!-- Calendar -->
    <div class="bg-white flex flex-col items-center px-[0.652rem] rounded-[0.948rem] w-full">
      <div class="flex flex-col gap-[0.474rem] items-start w-full">
        
        <!-- Day Headers -->
        <div class="flex gap-[0.711rem] items-start w-full">
          <div v-for="day in weekDays" :key="day" 
            class="flex-1 h-[2.431rem] flex items-center justify-center">
            <p class="font-['PfStardust30S'] leading-normal text-[#DFA67B] text-[1.25rem] text-center tracking-[-0.05rem]">
              {{ day }}
            </p>
          </div>
        </div>

        <!-- Calendar Rows -->
        <div v-for="(week, weekIdx) in calendarWeeks" :key="weekIdx" 
          class="flex gap-[0.711rem] items-start w-full">
          <div v-for="(dateObj, dayIdx) in week" :key="dayIdx"
            class="flex-1 h-[2.431rem] rounded-full cursor-pointer flex items-center justify-center"
            :class="getDayClasses(dateObj)"
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

    <!-- Legend -->
    <div class="flex items-center px-[0.948rem] py-[0.474rem] w-full">
      <div class="flex gap-[0.356rem] items-center">
        <div class="h-[0.729rem] w-[0.693rem]">
          <img 
            alt="calendar icon" 
            class="block max-w-none size-full" 
            src="/src/assets/icons/calendar.svg" 
          />
        </div>
        <p class="font-['PfStardust30S'] leading-normal text-[#805143] text-[1.25rem] tracking-[-0.05rem]">
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

// 주 단위로 달력 데이터 구성
const calendarWeeks = computed(() => {
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

const getDayClasses = (dateObj: { date: Date; isCurrentMonth: boolean }) => {
  const isSelected = isSameWeek(dateObj.date, props.selectedDate);
  const isHovered = isHoveredWeek(dateObj.date);
  
  if (!dateObj.isCurrentMonth) {
    return 'bg-white opacity-30';
  }
  
  if (isSelected) {
    return 'bg-[#FFF2CC] border border-[#FFD966]';
  }
  
  if (isHovered && dateObj.isCurrentMonth) {
    return 'bg-gray-200';
  }
  
  return 'bg-white';
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