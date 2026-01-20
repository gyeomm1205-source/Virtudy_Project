# 1. 베이스 이미지 (자바 버전 선택)
FROM openjdk:17-jdk-slim

# 2. 내 빌드된 Jar 파일을 이미지 안으로 복사
COPY build/libs/*-SNAPSHOT.jar app.jar

# 3. 실행 명령어
ENTRYPOINT ["java", "-jar", "app.jar"]