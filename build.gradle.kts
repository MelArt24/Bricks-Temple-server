plugins {
    kotlin("jvm") version "2.0.20"
    id("io.ktor.plugin") version "3.0.0"
    application
    kotlin("plugin.serialization") version "1.9.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("com.brickstemple.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    val ktorVersion = "3.0.0"
    val logbackVersion = "1.5.6"
    val exposedVersion = "0.53.0"
    val postgresVersion = "42.7.2"

    // Ktor server core
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")

    // Content negotiation + JSON serialization
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // Database (Exposed ORM + PostgreSQL)
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("org.postgresql:postgresql:$postgresVersion")

    // .env support
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    // Tests
    testImplementation("io.ktor:ktor-server-tests-jvm:2.3.9")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation(kotlin("test"))

    // ContentNegotiation + JSON
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // BCrypt
    implementation("org.mindrot:jbcrypt:0.4")

    // JWT + Authentication (Ktor 3.0.0)
    implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktorVersion")

    // JSON Web Tokens (Auth0 Java JWT)
    implementation("com.auth0:java-jwt:4.4.0")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}