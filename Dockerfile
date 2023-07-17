#FROM ubuntu:latest
#LABEL authors="namkyungmin"
#
#ENTRYPOINT ["top", "-b"]
FROM openjdk:11
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
