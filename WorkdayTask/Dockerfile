FROM openjdk:8-jdk-alpine
LABEL maintainer="eldar jafarov"
VOLUME /main-app
ADD target/task-0.0.1-SNAPSHOT.jar book-test.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "book-test.jar"]