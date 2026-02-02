import io
import base64
from PIL import Image
from fastapi import HTTPException

def process_image(image_file: bytes) -> str:
    try:
        image = Image.open(io.BytesIO(image_file))
        max_size = 512 # 700 -> 512 (GPT Vision 비용 절감 및 속도)

        if max(image.size) > max_size:
            image.thumbnail((max_size, max_size))

        if image.mode in ("RGBA", "P"):
            image = image.convert("RGB")

        buffered = io.BytesIO()
        image.save(buffered, format="JPEG", quality=85)
        return base64.b64encode(buffered.getvalue()).decode('utf-8')
    except Exception as e:
        print(f"이미지 처리 오류: {e}")
        raise HTTPException(status_code=400, detail="유효하지 않은 이미지 파일입니다.")