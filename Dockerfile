FROM gradle:jdk17-alpine AS build

WORKDIR /home/gradle/src

COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
COPY src ./src

RUN ./gradlew build -x test

FROM amazoncorretto:17-alpine

WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/workout-logger-api-1.1.0.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]