from pydantic import BaseModel

# GPT 응답 및 클라이언트 응답용
class AvatarResponse(BaseModel):
    member_id: int
    hair_front: str
    hair_back: str
    hair_color: str
    eyes: str
    glasses: str  # JSON key는 accessory_glasses지만 내부적으론 glasses로 통일 권장
    clothes: str
    clothes_color: str
    face_shape: str

    class Config:
        from_attributes = True # ORM 객체를 Pydantic 모델로 변환 허용