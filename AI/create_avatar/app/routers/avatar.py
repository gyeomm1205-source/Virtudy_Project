from fastapi import APIRouter, UploadFile, File, Form, Depends, HTTPException
from sqlalchemy.orm import Session

# ▼▼▼ 여기가 수정되었습니다 (점 2개 .. 사용) ▼▼▼
from ..database import get_db
from ..models import Avatar, Member
from ..schemas import AvatarResponse
from ..services import image_service, gpt_service
# ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

router = APIRouter()

@router.post("/avatar", response_model=AvatarResponse)
async def make_avatar(
    nick_name: str  = Form(...), 
    file: UploadFile = File(...), 
    db: Session = Depends(get_db)
):
    # 1. 이미지 처리
    content = await file.read()
    base64_image = image_service.process_image(content)

    # 2. GPT 분석
    gpt_result = await gpt_service.analyze_avatar_image(base64_image)

    user_id = find_member_id_by_nickname(nick_name, db)

    # 3. 데이터 매핑
    avatar_data = {
        "member_id": user_id,
        "hair_front": gpt_result.get("hair_front"),
        "hair_back": gpt_result.get("hair_back"),
        "hair_color": gpt_result.get("hair_color"),
        "eyes": gpt_result.get("eyes"),
        "glasses": gpt_result.get("accessory_glasses"),
        "clothes": gpt_result.get("outfit"),
        "clothes_color": gpt_result.get("clothes_color")
    }

    
    # 4. DB 저장 (Upsert)
    existing_avatar = db.query(Avatar).filter(Avatar.member_id == user_id).first()

    if existing_avatar:
        for key, value in avatar_data.items():
            setattr(existing_avatar, key, value)
        db.commit()
        db.refresh(existing_avatar)
        return existing_avatar
    else:
        new_avatar = Avatar(**avatar_data)
        db.add(new_avatar)
        db.commit()
        db.refresh(new_avatar)
        return new_avatar
    
# 예시: 닉네임을 받아서 member_id(UUID)를 찾는 로직
def find_member_id_by_nickname(nickname_input: str, db: Session):
    # 1. DB에서 닉네임으로 조회
    member = db.query(Member).filter(Member.nick_name == nickname_input).first()
    
    if not member:
        raise HTTPException(status_code=404, detail="해당 닉네임의 유저를 찾을 수 없습니다.")
    
    # 2. member_id (String UUID) 반환
    return member.id