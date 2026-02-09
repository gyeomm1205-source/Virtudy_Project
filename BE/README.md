# Virtudy Backend Server

## 📖 프로젝트 개요

**Virtudy**의 백엔드 서버 저장소입니다.

사용자의 학습 활동을 기록하고, 실시간 화상 스터디 룸을 제공하며, 학습 데이터에 기반한 리포트 및 랭킹 서비스를 제공합니다.

Spring Boot 기반의 안정적인 서버 환경 위에서 WebSocket과 Kafka를 활용한 실시간 데이터 파이프라인을 구축하였습니다.

---

## 🛠 기술 스택 (Tech Stack)

### **Core & Framework**

- **Java**: 17 (LTS)
- **Spring Boot**: 3.5.9
- **Gradle**: 빌드 및 의존성 관리

### **Database & Caching**

- **MySQL**: 메인 관계형 데이터베이스
    - JPA 기반 기본 쿼리 작성
    - JDBC 기반 Bulk Update 쿼리 작성
- **Redis**:
    - 실시간 학습 시간 캐싱
    - 세션 정보 관리
    - JWT 토큰 관리 (Refresh Token)

### **Messaging & Async Architecture**

- **Apache Kafka**:
    - AI 서버 및 타 마이크로서비스 간의 비동기 데이터 통신
    - 학습 상태 변경 이벤트 발행 및 소비
- **WebSocket (Stomp)**:
    - 클라이언트와의 양방향 실시간 통신
    - 스터디 룸 상태 동기화

### **External API & Solutions**

- **LiveKit**: WebRTC 기반 실시간 화상/음성 통신 서버 연동
- **Spring Actuator & Prometheus**: 애플리케이션 모니터링
- **Swagger (SpringDoc)**: REST API 명세서 자동 생성

---

## 📂 프로젝트 구조 (Package Structure)

기능별 패키지 구조(Package-by-Feature)를 채택하여 응집도를 높였으며, `global` 패키지에서 공통 관심사(Cross-cutting Concerns)를 관리합니다.

```
com.ssafy.virtudy
├── 📂 global           # 전역 설정 및 공통 유틸리티
│   ├── 📂 auth         # Security, JWT, OAuth2 (Kakao) 핸들링
│   ├── 📂 config       # WebSocket, WebMvc, Redis, Kafka 설정 클래스
│   ├── 📂 exception    # 전역 예외 처리 (GlobalExceptionHandler)
│   ├── 📂 aop          # 로깅 및 부가 기능 AOP
│   └── 📂 util         # 공통 유틸리티
│
├── 📂 member           # 회원 도메인
│   ├── 📂 controller   # 회원 API (로그인, 회원가입, 정보 수정)
│   ├── 📂 service      # 비즈니스 로직 (AuthService, MemberService)
│   └── 📂 domain       # Member Entity (Social Login 정보 포함)
│
├── 📂 study            # 스터디 도메인 (핵심 기능)
│   ├── 📂 controller   # 스터디 룸 생성/조회, 입장/퇴장 API
│   ├── 📂 service      # LiveKit 토큰 발급, 학습 시간 기록, 세션 관리
│   ├── 📂 domain       # StudyRoom, StudyLog, StudySession
│   └── 📂 consumer     # Kafka Consumer (학습 데이터 수신 및 처리)
│
├── 📂 group            # 스터디 그룹 도메인
│   ├── 📂 controller   # 그룹 CRUD API
│   └── 📂 service      # 그룹 멤버 관리 및 활동 로직
│
├── 📂 report           # 리포트 도메인
│   ├── 📂 controller   # 주간/월간 리포트 조회 API
│   ├── 📂 service      # AI 분석 데이터 기반 리포트 생성 및 저장 로직
│   └── 📂 scheduler    # 리포트 자동 생성 스케줄러 (Batch Job)
│
├── 📂 tier             # 티어(등급) 도메인
│   ├── 📂 service      # 티어 산정 및 점수 계산 로직
│   └── 📂 scheduler    # 1분 단위 티어 업데이트 스케줄러
│
└── 📂 rank             # 랭킹 도메인
    ├── 📂 controller   # 랭킹 조회 API
    └── 📂 service      # 개인/그룹 랭킹 집계 로직
```

---

## ✨ 세부 기능 (Detailed Features)

### 1. 🔐 인증 및 보안 (Authentication & Security)

- **JWT (Json Web Token)**: Access Token과 Redis 기반의 Refresh Token을 이용한 무상태(Stateless) 인증.
- **OAuth2 소셜 로그인**: Kakao 로그인을 지원하며, 신규 유저는 추가 정보 입력을 통해 회원가입 절차를 완료합니다.
- **보안 설정**: Spring Security를 통한 엔드포인트 별 권한 제어 (SecurityConfig).

### 2. 📹 실시간 화상 스터디 (Live Study)

- **LiveKit 연동**: 스터디 룸 입장 시 LiveKit 서버 접속을 위한 토큰을 동적으로 발급합니다.
- **실시간 상태 동기화**: WebSocket(Stomp)을 통해 유저의 입장/퇴장, 마이크/카메라 상태, 공부 중/휴식 중 상태를 실시간으로 브로드캐스팅합니다.
- **세션 관리**: Redis를 활용하여 현재 활성화된 세션 정보를 고속으로 처리합니다.
- **세션 종료 감지:** 비정상 이탈 시에도, 소켓 연결 종료를 자동으로 감지하여 세션 상태를 만료로 업데이트합니다.

### 3. 📊 데이터 파이프라인 (Data Pipeline)

- **학습 로그 처리**: 사용자의 학습 시작/종료 이벤트를 Kafka로 발행하고, AI 서버에서 분석된 순수 공부 시간(Real Study Time) 데이터를 다시 Consumer가 받아 DB에 저장합니다.
- **고성능 버퍼링 전략**: Redis Dual-Buffer 패턴을 적용하여 초당 수천 건의 AI 분석 로그를 손실 없이 버퍼링하고, Bulk Insert로 DB 쓰기 성능을 극대화했습니다.
- **이벤트 기반 아키텍처**: 서비스 간 결합도를 낮추고 대용량 트래픽에 유연하게 대응합니다.

### 4. 📈 리포트 및 분석 (Report & Analysis)

- **주간 리포트**: 사용자의 학습 패턴(시간대, 집중도 등)을 분석하여 시각화된 리포트 데이터를 생성합니다.
- **AI 기반 심층 분석**: AI 모델이 분석한 순수 집중 시간 데이터를 Spring Batch로 집계하여, 단순 통계를 넘어선 학습 몰입도 지표와 개선 피드백을 제공합니다.
- **스케줄링**: 매주 월요일 04시에 배치를 돌아 리포트를 자동 생성합니다.

### 5. 🏆 게이미피케이션 (Gamification)

- **티어 시스템**: 누적 학습 시간과 출석률 등을 종합하여 사용자 티어(Bronze ~ Diamond)를 산정합니다.
- **초고속 랭킹 연산**: Redis Sorted Set(ZSet)을 활용하여 O(log N)의 시간 복잡도로 실시간 랭킹을 산출하며, 인메모리 연산으로 랭킹 변동을 즉시 반영합니다.
- **실시간 갱신**: 1분 단위 스케줄러(`TimerScheduler`)가 동작하여 실시간에 가까운 티어 변동을 반영합니다.
- **랭킹 시스템**: 개인 랭킹과 스터디 그룹 랭킹을 제공하여 학습 동기를 부여합니다.

---

## 🚀 시작 가이드 (Getting Started)

### 1. 환경 설정 (Environment Setup)

프로젝트 실행을 위해 `application.yml`에 다음 설정이 필요합니다.

- DB 연결 정보 (MySQL, Redis)
- Kafka 브로커 주소
- LiveKit 서버 URL 및 API Key
- OAuth2 (Kakao) Client ID

### 2. 빌드 및 실행 (Build & Run)

```bash
# Gradle Wrapper로 빌드 (테스트 제외 시 -x test)
./gradlew build -x test

# 실행
java -jar build/libs/virtudy-0.0.1-SNAPSHOT.jar
```

### 3. API 문서 확인

서버 실행 후 브라우저에서 접속:

- `http://localhost:8080/swagger-ui/index.html`
