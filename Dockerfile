#FROM openjdk:17-jdk-slim
#
#WORKDIR /app
#
#COPY . .
#
#RUN ./gradlew build --no-daemon
#
#CMD ["java", "-jar", "build/libs/bricks-temple-server-all.jar"]


FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
