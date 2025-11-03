package com.brickstemple

import TokenResponse
import com.brickstemple.dto.UserDto
import com.brickstemple.fakeRepositories.FakeUserRepository
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class UserRoutesTest {

    @BeforeTest
    fun setEnv() {
        System.setProperty("JWT_SECRET", "test_secret")
    }

    private suspend fun loginAndGetToken(client: HttpClient, email: String, password: String): String {
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "$email", "password": "$password"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val json = response.bodyAsText()
        val tokenResponse = kotlinx.serialization.json.Json.decodeFromString<TokenResponse>(json)
        return tokenResponse.token.trim()
    }

    @Test
    fun `GET - users without token returns 401`() = testApplication {
        val repo = FakeUserRepository()

        application {
            module(testing = true, userRepo = repo)
        }

        val response = client.get("/users")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET - empty users returns 200 with token`() = testApplication {
        val repo = FakeUserRepository()

        repo.create(UserDto(username = "test", email = "test@example.com", password = "123456"))

        application {
            module(testing = true, userRepo = repo)
        }

        val token = loginAndGetToken(client, "test@example.com", "123456")

        val response = client.get("/users") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `GET - user by id returns 404`() = testApplication {
        val repo = FakeUserRepository()
        repo.create(UserDto(username = "test", email = "test@example.com", password = "123456"))

        application {
            module(testing = true, userRepo = repo)
        }

        val token = loginAndGetToken(client, "test@example.com", "123456")
        val response = client.get("/users/99") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `PUT - user can update self`() = testApplication {
        val repo = FakeUserRepository()
        val userId = repo.create(
            UserDto(username = "mark", email = "mark@example.com", password = "123456", role = "user")
        )

        application {
            module(testing = true, userRepo = repo)
        }

        val token = loginAndGetToken(client, "mark@example.com", "123456")

        val response = client.put("/users/$userId") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "username": "mark_updated",
                    "email": "mark2@example.com",
                    "password": "654321",
                    "role": "admin"
                }
                """.trimIndent()
            )
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `DELETE - admin can delete user`() = testApplication {
        val repo = FakeUserRepository()

        val adminId = repo.create(UserDto(username = "admin", email = "a@example.com", password = "123456", role = "admin"))
        val targetId = repo.create(UserDto(username = "bob", email = "b@example.com", password = "0000", role = "user"))

        application {
            module(testing = true, userRepo = repo)
        }

        val token = loginAndGetToken(client, "a@example.com", "123456")

        val response = client.delete("/users/$targetId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `DELETE - non-admin returns 403`() = testApplication {
        val repo = FakeUserRepository()
        val userId = repo.create(UserDto(username = "bob", email = "b@example.com", password = "0000", role = "user"))

        application {
            module(testing = true, userRepo = repo)
        }

        val token = loginAndGetToken(client, "b@example.com", "0000")

        val response = client.delete("/users/$userId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }
}
