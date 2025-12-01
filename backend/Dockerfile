FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -DskipTests=true -e dependency:go-offline
COPY src ./src
RUN mvn -DskipTests=true -e package spring-boot:repackage &&     echo '--- check jar has boot loader ---' &&     (jar tf target/musicband-backend-1.0.0.jar | grep -q 'org/springframework/boot/loader/Launcher.class' && echo 'OK: boot loader present' || (echo 'WARN: boot loader not found' && true))

FROM eclipse-temurin:21-jre
WORKDIR /app
ENV JAVA_OPTS=""
COPY --from=build /app/target/musicband-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
