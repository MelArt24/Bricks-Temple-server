FROM gradle:8.5-jdk17 AS build
WORKDIR /home/gradle/project

# Копіюємо лише файли конфігурації спочатку для кешування залежностей
COPY build.gradle.kts settings.gradle.kts ./
COPY . .

# Використовуємо --project-cache-dir, щоб винести всі кеші та тимчасові файли в локальну папку проєкту
# Це ізолює збірку від системних папок /root
RUN gradle build -x test --no-daemon --info --project-cache-dir /home/gradle/project/.gradle_cache

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Копіюємо тільки готовий результат
COPY --from=build /home/gradle/project/build/libs/bricks-temple-server-all.jar app.jar

# Вказуємо порт, який ми бачили в налаштуваннях DO
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]