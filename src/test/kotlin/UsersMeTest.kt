package com.brickstemple

import TokenResponse
import com.brickstemple.fakeRepositories.FakeUserRepository
import com.brickstemple.plugins.configureSecurity
import com.brickstemple.plugins.configureSerialization
import com.brickstemple.routes.authRoutes
import com.brickstemple.routes.userRoutes
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import com.brickstemple.dto.users.UserDto
import io.ktor.client.*
import io.ktor.server.routing.*

class UsersMeTest {

    @BeforeTest
    fun setup() {
        System.setProperty("JWT_SECRET", "test_secret")
    }

    private suspend fun login(client: HttpClient, email: String, password: String): String {
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{ "email": "$email", "password": "$password" }""")
        }
        val json = response.bodyAsText()
        val tokenResponse = kotlinx.serialization.json.Json.decodeFromString<TokenResponse>(json)
        return tokenResponse.token.trim()
    }

    @Test
    fun `GET - users me with valid token`() = testApplication {
        val repo = FakeUserRepository()
        repo.create(UserDto(username = "Bill Cipher", email = "weirdmageddon@example.com", password = "181920"))

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(repo)
                userRoutes(repo)
            }
        }

        val token = login(client, "weirdmageddon@example.com", "181920")

        val response = client.get("/users/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("weirdmageddon@example.com"))
    }

    @Test
    fun `GET - users me without token returns 401`() = testApplication {
        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(FakeUserRepository())
                userRoutes(FakeUserRepository())
            }
        }

        val response = client.get("/users/me")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET - users me with invalid token returns 401`() = testApplication {
        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(FakeUserRepository())
                userRoutes(FakeUserRepository())
            }
        }

        val response = client.get("/users/me") {
            header(HttpHeaders.Authorization, "Bearer invalid.token.here")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
