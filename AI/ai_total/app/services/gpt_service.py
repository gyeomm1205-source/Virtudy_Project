import json
from openai import AsyncOpenAI
from fastapi import HTTPException
from ..config import AVATAR_ANALYSIS_PROMPT, OPENAI_API_KEY

client = AsyncOpenAI(base_url="https://gms.ssafy.io/gmsapi/api.openai.com/v1")

async def analyze_avatar_image(base64_image: str) -> dict:
    try:
        response = await client.chat.completions.create(
            model="gpt-4o", # vision 기능이 포함된 최신 모델
            messages=[
                {"role": "system", "content": AVATAR_ANALYSIS_PROMPT},
                {
                    "role": "user",
                    "content": [
                        {"type": "text", "text": "이 사람의 특징을 분석해서 JSON으로 알려줘."},
                        {
                            "type": "image_url",
                            "image_url": {"url": f"data:image/jpeg;base64,{base64_image}"}
                        }
                    ]
                }
            ],
            response_format={"type": "json_object"},
            max_tokens=300,
            temperature=0.1
        )
        content = response.choices[0].message.content
        return json.loads(content)
    except Exception as e:
        print(f"GPT 호출 오류: {e}")
        raise HTTPException(status_code=500, detail="AI 분석 중 오류가 발생했습니다.")