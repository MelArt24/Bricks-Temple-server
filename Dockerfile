FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY . .

# Додаємо прапор -Dorg.gradle.kotlin.compiler.environment.keepalive=false
# Це змусить Kotlin завершувати процеси коректно без залишку "фантомних" файлів
RUN gradle build -x test --no-daemon -Dorg.gradle.kotlin.compiler.environment.keepalive=false

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/build/libs/bricks-temple-server-all.jar app.jar

CMD ["java", "-jar", "app.jar"]