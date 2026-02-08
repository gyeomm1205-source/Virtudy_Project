import os

# 환경 변수 로드 (python-dotenv 사용 권장)
DATABASE_URL = os.getenv("SQLALCHEMY_DATABASE_URL", "mysql+pymysql://root:ssafy1234@localhost:3306/virtudy") # 기본값 예시
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")

# GPT 시스템 프롬프트 (전체 내용)
AVATAR_ANALYSIS_PROMPT = """
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
- 얼굴 : "face_shape_default" 고정
- 안경 유무: "accessory_glasses" (안경을 썼으면 선택, 아니면 "none")
- 옷 종류: "outfit_shirt", "outfit_knit", "outfit_round_neck" 중 택1 (가장 비슷한 것)
- 옷 색깔: 이미지에서 추출한 주요 색상 Hex Code

[응답 필드명 (JSON Key)]
hair_front, hair_back, hair_color, eyes, accessory_glasses, outfit, clothes_color, face_shape
"""