from sqlalchemy import Column, String, BigInteger, Integer
from .database import Base

class Avatar(Base):
    __tablename__ = "avatar"

    id = Column(BigInteger, primary_key=True, autoincrement=True, index=True)
    member_id = Column(BigInteger, nullable=False, unique=True) # 1:1 관계라면 unique 추천
    
    hair_front = Column(String(255))
    hair_back = Column(String(255))
    eyes = Column(String(255))
    glasses = Column(String(255)) # DTO와 이름 통일 (accessory_glasses -> glasses 등 확인 필요)
    hair_color = Column(String(255))
    clothes = Column(String(255)) # outfit -> clothes 매핑 주의
    clothes_color = Column(String(255))

class Member(Base):
    # Java의 @Table(name="member")와 매핑됨
    __tablename__ = "member"

    # Java: private Long id;
    id = Column(Integer, primary_key=True, index=True)

    # Java: private String memberId; (UUID) -> DB 컬럼명: member_id
    member_id = Column(String(255), unique=True, nullable=False, index=True)

    # Java: private String nickName; -> DB 컬럼명: nick_name
    # 닉네임으로 검색해야 하므로 여기를 잘 맞춰야 합니다.
    nick_name = Column(String(255), nullable=False)

    # 필요한 경우 다른 필드도 매핑 (일단 검색에 필요한 것만 있어도 됨)
    email = Column(String(255), unique=True, nullable=False)
    
    # 아바타 생성 횟수 (Java: avatarGenCount -> DB: avatar_gen_count)
    avatar_gen_count = Column(Integer, default=0)

    # 나머지 필드는 필요할 때 추가하면 됩니다.
    # JPA가 만들어둔 테이블을 읽기만 할 거라면, 
    # 모든 컬럼을 다 적을 필요는 없고 필요한 컬럼(id, member_id, nick_name)만 있어도 됩니다.