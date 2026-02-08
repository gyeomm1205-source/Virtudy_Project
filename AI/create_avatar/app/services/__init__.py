# 가상환경 세팅 및 초기 설정. 
from openai import AsyncOpenAI
import os
import getpass


gms_key = os.getenv("SSAFY_GMS_API_KEY")

if gms_key:
    os.environ["OPENAI_API_KEY"] = gms_key
    print(" 환경 변수에서 SSAFY_GMS_API_KEY를 로드하여 설정했습니다.")

if not os.environ.get("OPENAI_API_KEY"):
    os.environ["OPENAI_API_KEY"] = getpass.getpass("GMS KEY를 입력하세요: ")

client = AsyncOpenAI(base_url="https://gms.ssafy.io/gmsapi/api.openai.com/v1")
print("OpenAI 클라이언트 설정이 완료되었습니다.")



