package com.brickstemple

import com.brickstemple.fakeRepositories.FakeUserRepository
import com.brickstemple.plugins.configureSerialization
import com.brickstemple.routes.authRoutes
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import com.brickstemple.dto.UserDto
import io.ktor.server.routing.*


class AuthRoutesTest {

    @BeforeTest
    fun setup() {
        System.setProperty("JWT_SECRET", "test_secret")
    }

    @Test
    fun `POST register - success`() = testApplication {
        val repo = FakeUserRepository()
        application {
            configureSerialization()
            routing { authRoutes(repo) }
        }

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{
                "username": "artem",
                "email": "artem@example.com",
                "password": "123456"
            }""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(response.bodyAsText().contains("User registered successfully"))
    }

    @Test
    fun `POST register - email already exists`() = testApplication {
        val repo = FakeUserRepository()
        repo.create(UserDto(username = "artem", email = "artem@example.com", password = "123456"))

        application {
            configureSerialization()
            routing { authRoutes(repo) }
        }

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{
                "username": "artem2",
                "email": "artem@example.com",
                "password": "123456"
            }""")
        }

        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `POST register - bad email`() = testApplication {
        application {
            configureSerialization()
            routing { authRoutes(FakeUserRepository()) }
        }

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody("""{
                "username": "test",
                "email": "not-an-email",
                "password": "123456"
            }""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST login - success`() = testApplication {
        val repo = FakeUserRepository()
        repo.create(UserDto(username = "artem", email = "artem@example.com", password = "123456"))

        application {
            configureSerialization()
            routing { authRoutes(repo) }
        }

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{ "email": "artem@example.com", "password": "123456" }""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("token"))
    }

    @Test
    fun `POST login - wrong password`() = testApplication {
        val repo = FakeUserRepository()
        repo.create(UserDto(username = "artem", email = "artem@example.com", password = "123456"))

        application {
            configureSerialization()
            routing { authRoutes(repo) }
        }

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{ "email": "artem@example.com", "password": "wrongpass" }""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST login - user not found`() = testApplication {
        application {
            configureSerialization()
            routing { authRoutes(FakeUserRepository()) }
        }

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{ "email": "none@example.com", "password": "123456" }""")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `POST login - invalid json`() = testApplication {
        application {
            configureSerialization()
            routing { authRoutes(FakeUserRepository()) }
        }

        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{ "email": "bad" }""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
