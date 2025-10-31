package com.brickstemple

import com.brickstemple.dto.UserDto
import com.brickstemple.plugins.configureSerialization
import com.brickstemple.routes.userRoutes
import com.brickstemple.fakeRepositories.FakeUserRepository
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import io.ktor.server.routing.routing


class UserRoutesTest {

    @Test
    fun `GET - empty users returns message`() = testApplication {
        application {
            configureSerialization()
            routing { userRoutes(FakeUserRepository()) }
        }
        val response = client.get("/users")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("No users"))
    }

    @Test
    fun `POST - create user`() = testApplication {
        val repo = FakeUserRepository()
        application {
            configureSerialization()
            routing { userRoutes(repo) }
        }
        val response = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody("""{"username": "john", "email": "john@example.com", "password": "12345", "role": "user"}""")
        }
        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    fun `POST - missing fields should return 400`() = testApplication {
        application {
            configureSerialization()
            routing { userRoutes(FakeUserRepository()) }
        }
        val response = client.post("/users") {
            contentType(ContentType.Application.Json)
            setBody("""{ "email": "wrong@example.com" }""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `GET - user by id returns 404`() = testApplication {
        val repo = FakeUserRepository()
        application {
            configureSerialization()
            routing { userRoutes(repo) }
        }
        val response = client.get("/users/99")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `PUT - update existing user`() = testApplication {
        val repo = FakeUserRepository()
        repo.create(
            UserDto(username = "mark", email = "m@example.com", password = "1234", role = "user")
        )

        application {
            configureSerialization()
            routing { userRoutes(repo) }
        }

        val response = client.put("/users/1") {
            contentType(ContentType.Application.Json)
            setBody("""{
                "username": "mark_updated",
                "email": "mark2@example.com",
                "password": "0000",
                "role": "user"
            }""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `PUT - invalid ID returns 400`() = testApplication {
        application {
            configureSerialization()
            routing { userRoutes(FakeUserRepository()) }
        }
        val response = client.put("/users/abc") {
            contentType(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `DELETE - remove existing user`() = testApplication {
        val repo = FakeUserRepository()
        repo.create(UserDto(username = "alice", email = "a@example.com", password = "12345", role = "user"))
        application {
            configureSerialization()
            routing { userRoutes(repo) }
        }
        val response = client.delete("/users/1")
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `DELETE - user not found returns 404`() = testApplication {
        application {
            configureSerialization()
            routing { userRoutes(FakeUserRepository()) }
        }
        val response = client.delete("/users/99")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
