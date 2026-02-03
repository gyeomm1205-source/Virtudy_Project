import random
from datetime import datetime, timedelta
from faker import Faker
from sqlalchemy.orm import Session

# ==========================================
# [중요] 본인의 프로젝트 구조에 맞게 import 경로 수정 필요
# ==========================================
from app.database import SessionLocal, engine
from app.models import (
    Base, Member, RoomMember, MemberGameStat, MemberPreference,
    Avatar, Report, StudySession, StudyLog, StudyRoom
)
# Enum 타입들도 모델에서 import 하거나, 문자열로 들어간다면 생략 가능
# from app.models import AgreedType, JobType, MemberStatType, ... 

fake = Faker('ko_KR')  # 한국어 더미 데이터 생성

def init_db():
    # 테이블이 없다면 생성 (이미 있다면 생략됨)
    Base.metadata.create_all(bind=engine)

def generate_dummy_data():
    db: Session = SessionLocal()
    
    try:
        print("🚀 더미 데이터 생성을 시작합니다...")

        # ------------------------------------------------
        # 1. Member 생성 (가장 먼저 생성되어야 함)
        # ------------------------------------------------
        members = []
        for _ in range(10):  # 10명 생성
            member = Member(
                memberId=fake.user_name(),
                isAgreed="AGREED",  # 또는 Enum 값
                email=fake.email(),
                password="password123", # 실제로는 해싱된 비밀번호를 넣어야 로그인 가능
                nickName=fake.name(),
                jobType=random.choice(["STUDENT", "WORKER", "JOB_SEEKER"]),
                status="ACTIVE",
                createdAt=datetime.now(),
                lastModifiedAt=datetime.now()
                # favoriteRoomId는 아직 방이 없으므로 None
            )
            db.add(member)
            members.append(member)
        
        db.commit() # ID 생성을 위해 커밋
        
        # 방금 생성된 멤버들의 ID를 다시 조회 (Foreign Key 연결용)
        for m in members:
            db.refresh(m)

        print(f"✅ Member {len(members)}명 생성 완료")

        # ------------------------------------------------
        # 2. Avatar, GameStat, Preference (Member 1:1)
        # ------------------------------------------------
        for member in members:
            # Avatar
            avatar = Avatar(
                member_id=member.id,
                hair_front=random.choice(["bangs", "center", "none"]),
                hair_back=random.choice(["long", "short", "bob"]),
                eyes=random.choice(["round", "cat", "droopy"]),
                hair_color=fake.color_name(),
                glasses=random.choice(["round", "square", "none"]),
                clothes=random.choice(["hoodie", "shirt", "knit"]),
                clothes_color=fake.color_name()
            )
            db.add(avatar)

            # GameStat
            stat = MemberGameStat(
                statId=fake.uuid4(),
                member_id=member.id,
                point=random.randint(0, 1000),
                totalStudyTime=random.randint(0, 10000),
                tierScore=random.randint(0, 500)
            )
            db.add(stat)

            # Preference
            pref = MemberPreference(
                prefId=fake.uuid4(),
                member_id=member.id,
                averageHours=random.randint(1, 10),
                targetHours=random.randint(1, 12),
                activeTime=random.choice(["MORNING", "NIGHT", "DAWN"])
            )
            db.add(pref)

        db.commit()
        print("✅ Avatar, Stats, Preference 생성 완료")

        # ------------------------------------------------
        # 3. StudyRoom 생성 (Member가 Owner)
        # ------------------------------------------------
        rooms = []
        for _ in range(5): # 방 5개 생성
            owner = random.choice(members)
            room = StudyRoom(
                roomId=fake.uuid4(),
                owner_id=owner.id,
                title=f"{fake.word()} 스터디방",
                password=None,
                type="PUBLIC",
                description=fake.sentence(),
                region=fake.city(),
                roomTierScore=random.randint(100, 1000),
                status="ACTIVE",
                createdAt=datetime.now(),
                lastModifiedAt=datetime.now()
            )
            db.add(room)
            rooms.append(room)
        
        db.commit()
        for r in rooms: db.refresh(r)
        print(f"✅ StudyRoom {len(rooms)}개 생성 완료")

        # ------------------------------------------------
        # 4. RoomMember (Member <-> Room 연결)
        # ------------------------------------------------
        for room in rooms:
            # 방장은 무조건 가입
            db.add(RoomMember(
                roomMemberId=fake.uuid4(),
                room_id=room.id,
                member_id=room.owner_id,
                joinedAt=datetime.now()
            ))
            
            # 랜덤 멤버 추가 가입
            guest = random.choice(members)
            if guest.id != room.owner_id:
                db.add(RoomMember(
                    roomMemberId=fake.uuid4(),
                    room_id=room.id,
                    member_id=guest.id,
                    joinedAt=datetime.now()
                ))

        db.commit()
        print("✅ RoomMember 연결 완료")

        # ------------------------------------------------
        # 5. StudySession & StudyLog & Report
        # ------------------------------------------------
        for member in members:
            # Report 생성
            db.add(Report(
                reportId=fake.uuid4(),
                member_id=member.id,
                reportDate=datetime.now().date(),
                endurance=random.randint(1, 100),
                focusDepth=random.randint(1, 100),
                regularity=random.randint(1, 100),
                stability=random.randint(1, 100),
                willPower=random.randint(1, 100),
                aiComment=fake.sentence()
            ))

            # Session 생성 (랜덤 방에서 공부했다고 가정)
            target_room = random.choice(rooms)
            session = StudySession(
                sessionId=fake.uuid4(),
                member_id=member.id,
                room_id=target_room.id,
                startTime=datetime.now() - timedelta(hours=2),
                endTime=datetime.now(),
                sessionRealStudyTime=7200,
                createdAt=datetime.now(),
                lastModifiedAt=datetime.now()
            )
            db.add(session)
            db.commit()
            db.refresh(session)

            # Log 생성
            db.add(StudyLog(
                logId=fake.uuid4(),
                session_id=session.id,
                member_id=member.id,
                eventType="FOCUS_START",
                detectedAt=datetime.now() - timedelta(hours=1)
            ))

        db.commit()
        print("✅ Session, Log, Report 생성 완료")
        
        print("\n🎉 모든 더미 데이터 생성이 완료되었습니다!")

    except Exception as e:
        print(f"❌ 에러 발생: {e}")
        db.rollback()
    finally:
        db.close()

if __name__ == "__main__":
    init_db()
    generate_dummy_data()