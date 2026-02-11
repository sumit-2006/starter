FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN chmod +x ./gradlew

COPY src src

RUN ./gradlew shadowJar --no-daemon -xtest

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY  --from=build /app/build/libs/*-fat.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
