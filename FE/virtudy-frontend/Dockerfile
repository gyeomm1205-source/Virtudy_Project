FROM nginx:stable-alpine

# 1. Nginx 기본 설정 제거 (기존 Dockerfile 로직 유지)
RUN rm /etc/nginx/conf.d/default.conf

# 2. 커스텀 Nginx 설정 복사 (기존 Dockerfile 로직 유지)
# (프로젝트 루트에 nginx.conf 파일이 존재해야 합니다)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 3. Jenkins가 빌드한 결과물(dist)을 이미지로 복사
# (기존의 COPY --from=build ... 부분을 대체)
COPY dist/ /usr/share/nginx/html/

# 4. 포트 개방 및 실행
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]