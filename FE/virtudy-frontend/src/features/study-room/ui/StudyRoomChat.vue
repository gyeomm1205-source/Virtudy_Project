<script setup lang="ts">
import { ref, nextTick, watch } from 'vue';

const props = defineProps<{
    messages: any[];
    userId: string;
    userNames: Record<string, string>; // ID -> 닉네임 맵
    onSendMessage: (msg: string) => void;
}>();


// 부모에게 닫기 이벤트 전송
const emit = defineEmits(['close']);
const chatMessage = ref('');
const chatListRef = ref<HTMLDivElement | null>(null);

const handleSendChat = () => {
    if (!chatMessage.value.trim()) return;
    props.onSendMessage(chatMessage.value);
    chatMessage.value = '';
};

const handleClose = () => {
    emit('close'); // 부모에게 "나 닫을게" 알림
};

// 스크롤 하단 이동
const scrollChatToBottom = () => {
    nextTick(() => {
        if (chatListRef.value) {
            chatListRef.value.scrollTop = chatListRef.value.scrollHeight;
        }
    });
};

watch(() => props.messages.length, scrollChatToBottom);
</script>

<template>
    <aside class="flex flex-col h-full w-80 min-w-80">
        <div class="bg-[var(--color-choco)] h-full flex flex-col overflow-hidden">
            <div class="flex items-center justify-center pt-1 px-1 pb-1 shrink-0">
                <div class="relative w-[19.9375rem] h-[3.25rem]">
                    <div class="absolute inset-0 bg-[var(--color-butter2)]"></div>
                    <div class="absolute left-[0.6875rem] top-1/2 transform -translate-y-1/2">
                        <h3 class="text-[var(--color-choco)] text-[2rem] font-['Xcu'] font-medium leading-normal">채팅</h3>
                    </div>
                    <button @click="handleClose" class="absolute right-2 top-1/2 transform -translate-y-1/2 w-6 h-6 flex items-center justify-center hover:opacity-70 transition-opacity">
                        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 36 36" fill="none"><path d="M21 19.5H22.5V21H24V22.5H25.5V24H27V25.5H28.5V27H30V28.5H31.5V30H33V31.5H31.5V33H30V31.5H28.5V30H27V28.5H25.5V27H24V25.5H22.5V24H21V22.5H19.5V21H16.5V22.5H15V24H13.5V25.5H12V27H10.5V28.5H9V30H7.5V31.5H6V33H4.5V31.5H3V30H4.5V28.5H6V27H7.5V25.5H9V24H10.5V22.5H12V21H13.5V19.5H15V16.5H13.5V15H12V13.5H10.5V12H9V10.5H7.5V9H6V7.5H4.5V6H3V4.5H4.5V3H6V4.5H7.5V6H9V7.5H10.5V9H12V10.5H13.5V12H15V13.5H16.5V15H19.5V13.5H21V12H22.5V10.5H24V9H25.5V7.5H27V6H28.5V4.5H30V3H31.5V4.5H33V6H31.5V7.5H30V9H28.5V10.5H27V12H25.5V13.5H24V15H22.5V16.5H21V19.5Z" fill="#DFA67B"/></svg>
                    </button>
                </div>
            </div>
            
            <div class="h-px bg-[var(--color-choco)] opacity-80 shrink-0"></div>

            <div ref="chatListRef" class="chat-list flex-1 overflow-y-auto p-6 space-y-2.5">
                <div v-for="(msg, idx) in messages" :key="idx" class="flex flex-col">
                    <div v-if="msg.type === 'CHAT'">
                        <div v-if="msg.sender === userId" class="flex justify-end">
                            <div class="bg-[var(--color-butter2)] px-4 py-1 rounded-xl max-w-64">
                                <p class="text-[var(--color-choco)] text-[1.3rem] font-['PfStardust30S'] leading-tight tracking-[-0.05rem]">{{ msg.data?.message || msg.message }}</p>
                            </div>
                        </div>
                        <div v-else class="flex flex-col space-y-1.5">
                            <p class="text-[var(--color-cream2)] text-[0.9375rem] font-['PfStardust30S'] leading-normal tracking-[-0.0375rem]">
                                {{ userNames[msg.sender] || msg.sender }}
                            </p>
                            <div class="bg-[var(--color-syrup)] px-4 py-1 rounded-xl max-w-64 w-fit">
                                <p class="text-[var(--color-cream2)] text-[1.3rem] font-['PfStardust30S'] leading-tight tracking-[-0.05rem]">{{ msg.data?.message || msg.message }}</p>
                            </div>
                        </div>
                    </div>
                    <div v-else-if="msg.type === 'SYSTEM'" class="flex justify-center my-2">
                        <div class="bg-black/20 px-3 py-1 rounded-full">
                            <p class="text-[var(--color-cream2)] text-xs opacity-90 font-['PfStardust30S']">
                                {{ msg.message }}
                            </p>
                        </div>
                    </div>
                </div>
            </div>

            <div class="p-[15px] shrink-0">
                <div class="bg-[#fff8e5] border-2 border-[#fff2cc] rounded-[12px] flex items-center justify-between h-[50px] px-[20px] py-[10px]">
                    <input 
                        v-model="chatMessage" 
                        @keyup.enter="handleSendChat" 
                        type="text" 
                        placeholder="메시지를 입력하세요" 
                        class="flex-1 bg-transparent text-[#805143] text-[20px] font-['PfStardust30S'] leading-normal tracking-[-0.8px] placeholder:opacity-40 outline-none"
                    />
                    <button @click="handleSendChat" class="w-6 h-6 hover:opacity-70 transition-opacity">
                        <img alt="send" class="w-full h-full" src="https://www.figma.com/api/mcp/asset/b9d70265-6324-4b42-bf69-7dfa3a62b17a" />
                    </button>
                </div>
            </div>
        </div>
    </aside>
</template>

<style scoped>
.chat-list {
    scrollbar-width: none;
    -ms-overflow-style: none;
}
.chat-list::-webkit-scrollbar {
    display: none;
}
</style>