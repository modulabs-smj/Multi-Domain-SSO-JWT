FROM amazoncorretto:17
LABEL maintainer="jsm5315@ajou.ac.kr"

ARG JAR_FILE=build/libs/location_share-0.0.1-SNAPSHOT.jar

WORKDIR /home/java/login

COPY ${JAR_FILE} /home/java/login/login-server.jar

EXPOSE 7000

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker","/home/java/login/login-server.jar"]