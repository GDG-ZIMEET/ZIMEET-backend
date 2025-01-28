FROM openjdk:17

# Docker 이미지 빌드 과정에서 외부에서 값을 전달 받아 설정하기 위함
ARG JAR_FILE=build/libs/*.jar

# JAR 파일 메인 디렉토리에 복사
COPY ${JAR_FILE} app.jar

ENV SPRING_PROFILES_ACTIVE=prod

# 시스템 진입점 정의
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "app.jar"]
