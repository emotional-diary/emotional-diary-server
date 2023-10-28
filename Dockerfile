#FROM ubuntu:latest
#LABEL authors="namgyeongmin"
#
#ENTRYPOINT ["top", "-b"]

# 도커 허브의 openjdk:11-jdk 설치
FROM openjdk:11-jdk

# CMD ./gradlew clean build
CMD ["./gradlew", "clean", "build"]

# JAR_FILE_PATH라는 변수에 프로젝트의 jar파일 경로 저장
ARG JAR_FILE_PATH=build/libs/EmotionalDiary-0.0.1-SNAPSHOT.jar

# 빌드 jar 파일을 app.jar로 복사
COPY ${JAR_FILE_PATH} app.jar

# java -jar app.jar를 실행
ENTRYPOINT ["java", "-jar", "app.jar"]