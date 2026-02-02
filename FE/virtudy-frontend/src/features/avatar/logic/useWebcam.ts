import { ref, onUnmounted } from 'vue';

export function useWebcam() {
  const videoRef = ref<HTMLVideoElement | null>(null);
  const stream = ref<MediaStream | null>(null);
  const isCameraOn = ref(false);

  // 카메라 켜기
  const startCamera = async () => {
    try {
      stream.value = await navigator.mediaDevices.getUserMedia({ 
        video: { facingMode: 'user', width: 640, height: 480 } 
      });
      if (videoRef.value) {
        videoRef.value.srcObject = stream.value;
        isCameraOn.value = true;
      }
    } catch (err) {
      console.error('카메라 접근 실패:', err);
      alert('카메라 권한을 허용해주세요! 📷');
    }
  };

  // 사진 찍기 (Video -> Canvas -> Blob -> File)
  const captureImage = (): Promise<File | null> => {
    return new Promise((resolve) => {
      if (!videoRef.value) return resolve(null);

      const canvas = document.createElement('canvas');
      canvas.width = videoRef.value.videoWidth;
      canvas.height = videoRef.value.videoHeight;
      
      const ctx = canvas.getContext('2d');
      if (!ctx) return resolve(null);

      // 좌우 반전해서 그리기
      ctx.translate(canvas.width, 0);
      ctx.scale(-1, 1);
      ctx.drawImage(videoRef.value, 0, 0);

      // 파일로 변환
      canvas.toBlob((blob) => {
        if (!blob) return resolve(null);
        const file = new File([blob], "avatar_capture.png", { type: "image/png" });
        resolve(file);
      }, 'image/png');
    });
  };

  // 카메라 끄기 (리소스 정리)
  const stopCamera = () => {
    if (stream.value) {
      stream.value.getTracks().forEach(track => track.stop());
      stream.value = null;
      isCameraOn.value = false;
    }
  };

  onUnmounted(() => {
    stopCamera();
  });

  return { videoRef, startCamera, stopCamera, captureImage, isCameraOn };
}