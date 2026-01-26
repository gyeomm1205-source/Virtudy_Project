from typing import Union
from fastapi import FastAPI, UploadFile, File, HTTPException
import make_avatar
from __init__ import client
import base64
import io
from PIL import Image
from pydantic import BaseModel
import os

app = FastAPI()


# constant 설정. 
BASE_PATH = "./assets"

ASSETS = {
    "hair_front": {
        "bang": [
            "hair_front_bangs_base.png",
            "hair_front_bangs_line.png",
            "hair_front_bangs_shadow.png"
        ],
        "center_part": [
            "hair_front_center_part_base.png",
            "hair_front_center_part_line.png",
            "hair_front_center_part_shadow.png"
        ],
        "hair_front_none" : [
            "hair_front_none_base.png",
            "hair_front_none_line.png",
            "hair_front_none_shadow.png"
        ],
        "hair_front_short" : [
            "hair_front_short_line.png",
            "hair_front_short_base.png",
            "hair_front_short_shadow.png"
        ],
        "hair_front_side_part": [
            "hair_front_side_part_base.png",
            "hair_front_side_part_line.png",
            "hair_front_side_part_shadow.png"
        ]
    },
    "hair_back" : {
        "hair_back_bob" : [
            "hair_back_bob_base.png",
            "hair_back_bob_line.png",
            "hair_back_bob_shadow.png"
        ],
        "hair_back_long_curly": [
            "hair_back_long_curly_base.png",
            "hair_back_long_curly_line.png",
            "hair_back_long_curly_shadow.png"
        ],
        "hair_back_long_straight" : [
            "hair_back_long_straight_base.png",
            "hair_back_long_straight_line.png",
            "hair_back_long_straight_shadow.png"
        ],
        "hair_back_long_lowtail" : [
            "hair_back_long_lowtail_line.png",
            "hair_back_long_lowtail_shadow.png",
            "hair_back_long_lowtail_base.png"
        ],
        "hair_back_short" : [
            "hair_back_short_base.png",
            "hair_back_short_line.png",
            "hair_back_short_shadow.png"
        ]
    },
    "eyes" : {
        "eyes_cat" : [
            "eyes_cat_base.png",
            "eyes_cat_line.png"
        ],
        "eyes_droopy" : [
            "eyes_droopy_base.png",
            "eyes_droopy_line.png"
        ],
        "eyes_round" : [
            "eyes_round_base.png",
            "eyes_round_line.png"
        ]
    },
    "accessory_glasses" : {
        "accessory_glasses" : [
            "accessory_glasses_default.png"
        ]
    },
    "outfit" : {
        "outfit_knit": [
            "outfit_knit_line.png"
        ],
        "outfit_round_neck": [
            "outfit_round_neck_line.png"
        ],
        "outfit_shirt" : [
            "outfit_shirt_line.png"
        ]
    },
    "face_shape" : {
        "face_shape_default": [
            "face_shape_default_base.png",
            "face_shape_default_line.png",
            "face_shape_default_shadow.png"
        ]
    }
    
}

LAYER_ORDER = [
    "hair_back",   # 가장 뒤
    "face_shape",     # 기본 몸통 (항상 존재한다고 가정)
    "eyes",
    "outfit",
    "hair_front",
    "accessory_glasses" # 가장 앞
]

class AvatarResponse(BaseModel):
    hair_front: str     # 앞머리
    hair_back : str     # 뒷머리
    hair_color : str    # 머리색 
    eyes : str          # 눈 모양
    accessory_glasses: str        # 안경 유무
    outfit: str   #옷 종류
    clothes_color : str # 옷 색깔
    face_shape : str

def process_image(image_file: bytes) -> str: 
    try :
        image = Image.open(io.BytesIO(image_file))

        # 이미지 리사이징 (긴 쪽을 512px로 맞춤 - 토큰 절약 및 속도 향상)
        max_size = 700

        if max(image.size) > max_size: 
            image.thumbnail((max_size, max_size))

        
        # 투명 배경 (PNG) 대응 : RGBA -> RGB 변환
        if image.mode in ("RGBA", "P"):
            image = image.convert("RGB")

        # 메모리 버퍼에 JPEG로 저장
        buffered = io.BytesIO()
        image.save(buffered, format="JPEG", quality=85) # 품질 85%로 압축
        
        # Base64 인코딩
        return base64.b64encode(buffered.getvalue()).decode('utf-8')    
    except Exception as e:
        print(f"이미지 처리 중 오류: {e}")
        raise HTTPException(status_code=400, detail="이미지 처리 실패")

# 이미지를 받으면 그걸 바탕으로 아바타를 생성해주는 API
@app.post("/makeAvatar", response_model=AvatarResponse)
async def make_avatar(file:UploadFile = File(...)):

    # 1. 파일 읽기
    content = await file.read()

    # 2. 이미지 압축 및 인코딩 
    base64_image = process_image(content)

    # 3. AI 호출 
    result = await call_gpt_vision(base64_image)

    img = await create_character(result)
    return result 
    
async def call_gpt_vision(base64_image: str): 
    # 시스템 프롬프트: 규칙과 팔레트 정의
    system_prompt = """
    너는 이미지를 분석하여 아바타 생성을 위한 속성을 추출하는 전문가야.
    반드시 아래 JSON 형식으로만 응답해.

    [헤어 컬러 팔레트 (이 중에서 가장 비슷한 색의 Hex Code를 선택)]
    1. Jet Black (칠흑색): #111111
    2. Natural Black (자연 흑색): #2B2B2B
    3. Dark Brown (다크 브라운): #3B3024
    4. Choco Brown (초코 브라운): #4E3524
    5. Red Brown (레드 브라운): #5D3A29
    6. Orange Brown (오렌지 브라운): #825336
    7. Gold Brown (골드 브라운): #9A7248
    8. Ash Brown (애쉬 브라운): #605A54
    9. Ash Gray (애쉬 그레이/백발): #8C8C8C
    10. Blue Black (블루 블랙): #181B26

    [선택지 옵션]
    - 앞머리: "hair_front_none", "center_part", "bang", "hair_front_side_part", "hair_front_short" 중 택1
    - 뒷머리: "hair_back_short", "hair_back_bob", "hair_back_long_straight", "hair_back_long_curly", "hair_back_long_lowtail" 중 택1
    - 눈: "eyes_cat", "eyes_droopy", "eyes_round" 중 택1
    - 얼굴 : "face_shape_default" 택
    - 안경 유무: "accessory_glasses" 중 택1
    - 옷 종류: "outfit_shirt", "outfit_knit", "outfit_round_neck" 중 택1 (가장 비슷한 것)
    - 옷 색깔: 이미지에서 추출한 주요 색상 Hex Code

    [응답 필드명 (JSON Key)]
    hair_front, hair_back, hair_color, eyes, accessory_glasses, outfit, clothes_color, face_shape
    """
    try:
        response = await client.chat.completions.create(
            model="gpt-4o-mini",  # gpt-4.1은 없습니다. vision 기능이 뛰어난 gpt-4o 사용 권장
            messages=[
                {
                    "role": "system",
                    "content": system_prompt
                },
                {
                    "role": "user",
                    "content": [
                        {"type": "text", "text": "이 사람의 특징을 분석해서 JSON으로 알려줘."},
                        {
                            "type": "image_url",
                            "image_url": {
                                "url": f"data:image/jpeg;base64,{base64_image}"
                            }
                        }
                    ]
                }
            ],
            response_format={ "type": "json_object" }, # JSON 모드 강제
            max_tokens=300,
            temperature=0.1 # 분석 결과의 일관성을 위해 낮춤
        )
        
        # 문자열로 된 JSON을 파이썬 딕셔너리로 변환하여 반환
        import json
        content = response.choices[0].message.content
        return json.loads(content)
    except Exception as e:
        print(f"GPT 호출 중 오류: {e}")
        raise HTTPException(status_code=500, detail="AI 분석 실패")
    
async def create_character(response:str):
    """
    데이터(JSON)을 받아서 이미지를 조립하는 메인 함수
    """

    canvas_size = (200,200)
    canvas = Image.new("RGBA", canvas_size, (0, 0, 0, 0))
    # 앞머리 

    # --- 핵심 로직 : 정의된 순서대로 레이어 쌓기 ---
    for layer_name in LAYER_ORDER:
        item_name = response.get(layer_name)
        if not item_name or item_name in ["없음", "무"]:
            continue
        
        layer_info = ASSETS[layer_name]
        if not layer_info : continue 
        print("asdfasdfasdf", layer_info )
        # 3. 파일 경로 생성 
        for layer_path in ASSETS[layer_name][item_name] : 
            print(layer_path)
            file_path = os.path.join(BASE_PATH, layer_name,layer_path)
        
            if not os.path.exists(file_path) : 
                print(f"[Skip] 파일을 찾을 수 없음: {file_path}")
                continue
            
            try : 
                with Image.open(file_path) as img: 
                    img = img.convert("RGBA")

                    # 리사이징 필요 시 
                    if img.size != canvas_size:
                        img = img.resize(canvas_size, Image.LANCZOS)

                    # # 4. 색상 적용 로직 
                    # if color_key and col

                    # 5. 캔버스에 붙이기 
                    canvas.paste(img, (0, 0), mask=img)
                    print(f"[Layer] {layer_name} 병합 완료 ({item_name})")
            except Exception as e : 
                print(f"[Error] {layer_name} 처리 중 오류 : {e}")

    # 최종 저장 
    output_path = "completed_character.png"
    canvas.save(output_path, format="PNG")
    print(f"\n[Success] 저장 완료: {output_path}")
    return output_path
    
    # imagePath. 
    print(ASSETS['hair_front'][response['hair_front']])

    # 뒷머리 
    print(ASSETS['hair_back'][response['hair_back']])

    # 눈 
    print(ASSETS['eyes'][response['eyes']])

    # 안경
    print(ASSETS['glasses'][response['glasses']])

    # 외형
    print(ASSETS['outfit'][response['outfit']])

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)