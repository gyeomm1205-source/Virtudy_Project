import { nextTick, onUnmounted, ref, watch, type Ref } from 'vue';

export function useFocusTimer(shouldRun: Ref<boolean>) {
	const focusSeconds = ref(0);
	const isRunning = ref(false);
	let intervalId: ReturnType<typeof setInterval> | null = null;

	const start = () => {
		if (isRunning.value) return;
		isRunning.value = true;
		intervalId = setInterval(() => {
			focusSeconds.value += 1;
		}, 1000);
	};

	const pause = () => {
		if (!isRunning.value) return;
		isRunning.value = false;
		if (intervalId) {
			clearInterval(intervalId);
			intervalId = null;
		}
	};

	const reset = () => {
		pause();
		focusSeconds.value = 0;
	};

	watch(
		shouldRun,
		async (run) => {
			if (!run) {
				pause();
				return;
			}
			await nextTick();
			start();
		},
		{ immediate: true, flush: 'post' }
	);

	onUnmounted(() => {
		pause();
	});

	return {
		focusSeconds,
		isRunning,
		start,
		pause,
		reset,
	};
}