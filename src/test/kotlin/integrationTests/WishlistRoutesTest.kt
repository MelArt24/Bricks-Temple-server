package com.brickstemple.integrationTests

import integrationTests.TokenResponse
import com.brickstemple.dto.users.UserDto
import com.brickstemple.fakeRepositories.FakeWishlistRepository
import com.brickstemple.fakeRepositories.FakeWishlistItemRepository
import com.brickstemple.fakeRepositories.FakeUserRepository
import com.brickstemple.plugins.configureSecurity
import com.brickstemple.plugins.configureSerialization
import com.brickstemple.routes.authRoutes
import com.brickstemple.routes.wishlistRoutes
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.*


class WishlistRoutesTest {

    private suspend fun login(client: HttpClient, email: String, password: String): String {
        val response = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "$email", "password": "$password"}""")
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val json = response.bodyAsText()
        val token = Json.decodeFromString<TokenResponse>(json)
        return token.token.trim()
    }

    @Test
    fun `GET wishlist - empty returns message`() = testApplication {
        val userRepo = FakeUserRepository()
        val wishlistRepo = FakeWishlistRepository()
        val wishlistItemRepo = FakeWishlistItemRepository()

        userRepo.create(
            UserDto(
                username = "test",
                email = "test@mail.com",
                password = "123456"
            )
        )

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(userRepo)
                wishlistRoutes(wishlistRepo, wishlistItemRepo)
            }
        }

        val token = login(client, "test@mail.com", "123456")

        val response = client.get("/wishlist") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Wishlist is empty"))
    }

    @Test
    fun `POST wishlist add - creates wishlist and adds first item`() = testApplication {
        val userRepo = FakeUserRepository()
        val wishlistRepo = FakeWishlistRepository()
        val wishlistItemRepo = FakeWishlistItemRepository()

        userRepo.create(
            UserDto(
                username = "test",
                email = "test@mail.com",
                password = "123456"
            )
        )

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(userRepo)
                wishlistRoutes(wishlistRepo, wishlistItemRepo)
            }
        }

        val token = login(client, "test@mail.com", "123456")

        val response = client.post("/wishlist/add") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"productId": 101}""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val getAfter = client.get("/wishlist") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        val body = getAfter.bodyAsText()
        assertEquals(HttpStatusCode.OK, getAfter.status)
        assertTrue(body.contains("\"productId\": 101") || body.contains("\"productId\":101"))
        assertTrue(body.contains("\"quantity\": 1") || body.contains("\"quantity\":1"))
    }

    @Test
    fun `POST wishlist add - adding same product increments quantity`() = testApplication {
        val userRepo = FakeUserRepository()
        val wishlistRepo = FakeWishlistRepository()
        val wishlistItemRepo = FakeWishlistItemRepository()

        userRepo.create(
            UserDto(
                username = "test2",
                email = "test2@mail.com",
                password = "123456"
            )
        )

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(userRepo)
                wishlistRoutes(wishlistRepo, wishlistItemRepo)
            }
        }

        val token = login(client, "test2@mail.com", "123456")

        val r1 = client.post("/wishlist/add") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"productId": 77}""")
        }
        assertEquals(HttpStatusCode.Created, r1.status)

        val r2 = client.post("/wishlist/add") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"productId": 77}""")
        }
        assertEquals(HttpStatusCode.Created, r2.status)

        val getAfter = client.get("/wishlist") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        val body = getAfter.bodyAsText()
        assertEquals(HttpStatusCode.OK, getAfter.status)

        assertTrue(body.contains("\"productId\": 77") || body.contains("\"productId\":77"))
        assertTrue(body.contains("\"quantity\": 2") || body.contains("\"quantity\":2"))

        println("BODY: $body")

    }

    @Test
    fun `POST wishlist add - 400 when missing productId`() = testApplication {
        val userRepo = FakeUserRepository()
        val wishlistRepo = FakeWishlistRepository()
        val wishlistItemRepo = FakeWishlistItemRepository()

        userRepo.create(
            UserDto(
                username = "test3",
                email = "test3@mail.com",
                password = "123456"
            )
        )

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(userRepo)
                wishlistRoutes(wishlistRepo, wishlistItemRepo)
            }
        }

        val token = login(client, "test3@mail.com", "123456")

        val response = client.post("/wishlist/add") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"wrongField": 10}""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST wishlist add - 401 when no token`() = testApplication {
        val userRepo = FakeUserRepository()
        val wishlistRepo = FakeWishlistRepository()
        val wishlistItemRepo = FakeWishlistItemRepository()

        userRepo.create(
            UserDto(
                username = "userNoToken",
                email = "no-token@mail.com",
                password = "123456"
            )
        )

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(userRepo)
                wishlistRoutes(wishlistRepo, wishlistItemRepo)
            }
        }

        val response = client.post("/wishlist/add") {
            contentType(ContentType.Application.Json)
            setBody("""{"productId": 5}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `DELETE wishlist remove - quantity decreases when more than 1`() = testApplication {
        val userRepo = FakeUserRepository()
        val wishlistRepo = FakeWishlistRepository()
        val wishlistItemRepo = FakeWishlistItemRepository()

        userRepo.create(UserDto(username = "test4", email = "test4@mail.com", password = "123456"))

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(userRepo)
                wishlistRoutes(wishlistRepo, wishlistItemRepo)
            }
        }

        val token = login(client, "test4@mail.com", "123456")

        repeat(2) {
            client.post("/wishlist/add") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody("""{"productId": 55}""")
            }
        }

        val wishlistResponse = client.get("/wishlist") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        val itemId = Json.parseToJsonElement(wishlistResponse.bodyAsText())
            .jsonObject["items"]!!
            .jsonArray[0].jsonObject["id"]!!.toString().toInt()

        val deleteResp = client.delete("/wishlist/remove/$itemId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, deleteResp.status)

        val after = client.get("/wishlist") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertTrue(after.bodyAsText().contains("\"quantity\":1") || after.bodyAsText().contains("\"quantity\": 1"))
    }

    @Test
    fun `DELETE wishlist remove - item deleted when quantity becomes zero`() = testApplication {
        val userRepo = FakeUserRepository()
        val wishlistRepo = FakeWishlistRepository()
        val wishlistItemRepo = FakeWishlistItemRepository()

        userRepo.create(UserDto(username = "test5", email = "test5@mail.com", password = "123456"))

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(userRepo)
                wishlistRoutes(wishlistRepo, wishlistItemRepo)
            }
        }

        val token = login(client, "test5@mail.com", "123456")

        client.post("/wishlist/add") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"productId": 100}""")
        }

        val getBefore = client.get("/wishlist") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        val itemId = Json.parseToJsonElement(getBefore.bodyAsText())
            .jsonObject["items"]!!.jsonArray[0].jsonObject["id"]!!.toString().toInt()

        val deleteResp = client.delete("/wishlist/remove/$itemId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, deleteResp.status)

        val getAfter = client.get("/wishlist") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertTrue(getAfter.bodyAsText().contains("Wishlist is empty"))
    }

    @Test
    fun `DELETE wishlist remove - 404 if item not found`() = testApplication {
        val userRepo = FakeUserRepository()
        val wishlistRepo = FakeWishlistRepository()
        val wishlistItemRepo = FakeWishlistItemRepository()

        userRepo.create(UserDto(username = "test6", email = "test6@mail.com", password = "123456"))

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(userRepo)
                wishlistRoutes(wishlistRepo, wishlistItemRepo)
            }
        }

        val token = login(client, "test6@mail.com", "123456")

        val seed = client.post("/wishlist/add") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"productId": 1}""")
        }
        assertEquals(HttpStatusCode.Created, seed.status)

        val resp = client.delete("/wishlist/remove/999") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.NotFound, resp.status)
        assertTrue(resp.bodyAsText().contains("Item not found"))
    }

    @Test
    fun `DELETE wishlist remove - 403 if item belongs to another user`() = testApplication {
        val users = FakeUserRepository()
        val wishlistRepo = FakeWishlistRepository()
        val itemsRepo = FakeWishlistItemRepository()

        users.create(UserDto(username = "u1", email = "u1@mail.com", password = "pass"))
        users.create(UserDto(username = "u2", email = "u2@mail.com", password = "pass"))

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(users)
                wishlistRoutes(wishlistRepo, itemsRepo)
            }
        }

        val token1 = login(client, "u1@mail.com", "pass")
        client.post("/wishlist/add") {
            header(HttpHeaders.Authorization, "Bearer $token1")
            contentType(ContentType.Application.Json)
            setBody("""{"productId": 500}""")
        }

        val get = client.get("/wishlist") {
            header(HttpHeaders.Authorization, "Bearer $token1")
        }
        val itemId = Json.parseToJsonElement(get.bodyAsText())
            .jsonObject["items"]!!.jsonArray[0].jsonObject["id"]!!.toString().toInt()

        val token2 = login(client, "u2@mail.com", "pass")
        val resp = client.delete("/wishlist/remove/$itemId") {
            header(HttpHeaders.Authorization, "Bearer $token2")
        }

        assertTrue(
            resp.status == HttpStatusCode.Forbidden || resp.status == HttpStatusCode.NotFound,
            "Expected 403 or 404, got ${resp.status}"
        )
    }

    @Test
    fun `DELETE wishlist remove - 400 if id is not integer`() = testApplication {
        val userRepo = FakeUserRepository()
        val wishlistRepo = FakeWishlistRepository()
        val wishlistItemRepo = FakeWishlistItemRepository()

        userRepo.create(UserDto(username = "test7", email = "test7@mail.com", password = "123456"))

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(userRepo)
                wishlistRoutes(wishlistRepo, wishlistItemRepo)
            }
        }

        val token = login(client, "test7@mail.com", "123456")

        val resp = client.delete("/wishlist/remove/abc") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.BadRequest, resp.status)
    }

    @Test
    fun `DELETE wishlist remove - 401 if no token`() = testApplication {
        val wishlistRepo = FakeWishlistRepository()
        val wishlistItemRepo = FakeWishlistItemRepository()
        val users = FakeUserRepository()

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(users)
                wishlistRoutes(wishlistRepo, wishlistItemRepo)
            }
        }

        val resp = client.delete("/wishlist/remove/1")
        assertEquals(HttpStatusCode.Unauthorized, resp.status)
    }

    @Test
    fun `DELETE wishlist clear - removes all items`() = testApplication {
        val userRepo = FakeUserRepository()
        val wishlistRepo = FakeWishlistRepository()
        val wishlistItemRepo = FakeWishlistItemRepository()

        userRepo.create(UserDto(username = "clearUser", email = "clear@mail.com", password = "123456"))

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(userRepo)
                wishlistRoutes(wishlistRepo, wishlistItemRepo)
            }
        }

        val token = login(client, "clear@mail.com", "123456")

        repeat(3) {
            client.post("/wishlist/add") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody("""{"productId": $it}""")
            }
        }

        val resp = client.delete("/wishlist/clear") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, resp.status)
        assertTrue(resp.bodyAsText().contains("Wishlist cleared"))

        val after = client.get("/wishlist") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertTrue(after.bodyAsText().contains("Wishlist is empty"))
    }


    @Test
    fun `DELETE wishlist clear - when already empty returns OK`() = testApplication {
        val userRepo = FakeUserRepository()
        val wishlistRepo = FakeWishlistRepository()
        val wishlistItemRepo = FakeWishlistItemRepository()

        userRepo.create(UserDto(username = "emptyUser", email = "empty@mail.com", password = "123456"))

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(userRepo)
                wishlistRoutes(wishlistRepo, wishlistItemRepo)
            }
        }

        val token = login(client, "empty@mail.com", "123456")

        val resp = client.delete("/wishlist/clear") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, resp.status)
        assertTrue(resp.bodyAsText().contains("Wishlist is already empty"))
    }


    @Test
    fun `DELETE wishlist clear - 401 if no token`() = testApplication {
        val userRepo = FakeUserRepository()
        val wishlistRepo = FakeWishlistRepository()
        val wishlistItemRepo = FakeWishlistItemRepository()

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(userRepo)
                wishlistRoutes(wishlistRepo, wishlistItemRepo)
            }
        }

        val resp = client.delete("/wishlist/clear")
        assertEquals(HttpStatusCode.Unauthorized, resp.status)
    }


    @Test
    fun `PUT wishlist item id - updates quantity successfully`() = testApplication {
        val users = FakeUserRepository()
        val wishlistRepo = FakeWishlistRepository()
        val itemsRepo = FakeWishlistItemRepository()

        users.create(UserDto(username = "user1", email = "put@mail.com", password = "123456"))

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(users)
                wishlistRoutes(wishlistRepo, itemsRepo)
            }
        }

        val token = login(client, "put@mail.com", "123456")

        client.post("/wishlist/add") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"productId": 200}""")
        }

        val body = client.get("/wishlist") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.bodyAsText()
        val itemId = Json.parseToJsonElement(body)
            .jsonObject["items"]!!
            .jsonArray[0].jsonObject["id"]!!.jsonPrimitive.int

        val resp = client.put("/wishlist/item/$itemId") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"quantity": 5}""")
        }
        assertEquals(HttpStatusCode.OK, resp.status)
        assertTrue(resp.bodyAsText().contains("Quantity updated"))

        val after = client.get("/wishlist") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.bodyAsText()
        assertTrue(after.contains("\"quantity\": 5") || after.contains("\"quantity\":5"))
    }


    @Test
    fun `PUT wishlist item id - 400 if quantity is zero or negative`() = testApplication {
        val users = FakeUserRepository()
        val wishlistRepo = FakeWishlistRepository()
        val itemsRepo = FakeWishlistItemRepository()

        users.create(UserDto(username = "user2", email = "neg@mail.com", password = "123456"))

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(users)
                wishlistRoutes(wishlistRepo, itemsRepo)
            }
        }

        val token = login(client, "neg@mail.com", "123456")

        client.post("/wishlist/add") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"productId": 300}""")
        }

        val body = client.get("/wishlist") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.bodyAsText()
        val itemId = Json.parseToJsonElement(body)
            .jsonObject["items"]!!.jsonArray[0]
            .jsonObject["id"]!!.jsonPrimitive.int

        val resp = client.put("/wishlist/item/$itemId") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"quantity": 0}""")
        }

        assertEquals(HttpStatusCode.BadRequest, resp.status)
        assertTrue(resp.bodyAsText().contains("Quantity must be > 0"))
    }


    @Test
    fun `PUT wishlist item id - 404 if item not found`() = testApplication {
        val users = FakeUserRepository()
        val wishlistRepo = FakeWishlistRepository()
        val itemsRepo = FakeWishlistItemRepository()

        users.create(UserDto(username = "user3", email = "notfound@mail.com", password = "123456"))

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(users)
                wishlistRoutes(wishlistRepo, itemsRepo)
            }
        }

        val token = login(client, "notfound@mail.com", "123456")

        client.post("/wishlist/add") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"productId": 400}""")
        }

        val resp = client.put("/wishlist/item/999") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"quantity": 3}""")
        }

        assertEquals(HttpStatusCode.NotFound, resp.status)
        assertTrue(resp.bodyAsText().contains("Item not found"))
    }


    @Test
    fun `PUT wishlist item id - 403 or 404 if item belongs to another user`() = testApplication {
        val users = FakeUserRepository()
        val wishlistRepo = FakeWishlistRepository()
        val itemsRepo = FakeWishlistItemRepository()

        users.create(UserDto(username = "u1", email = "owner@mail.com", password = "pass"))
        users.create(UserDto(username = "u2", email = "notowner@mail.com", password = "pass"))

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(users)
                wishlistRoutes(wishlistRepo, itemsRepo)
            }
        }

        val token1 = login(client, "owner@mail.com", "pass")
        client.post("/wishlist/add") {
            header(HttpHeaders.Authorization, "Bearer $token1")
            contentType(ContentType.Application.Json)
            setBody("""{"productId": 600}""")
        }

        val body = client.get("/wishlist") {
            header(HttpHeaders.Authorization, "Bearer $token1")
        }.bodyAsText()

        val itemId = Json.parseToJsonElement(body)
            .jsonObject["items"]!!
            .jsonArray[0].jsonObject["id"]!!.jsonPrimitive.int

        val token2 = login(client, "notowner@mail.com", "pass")
        val resp = client.put("/wishlist/item/$itemId") {
            header(HttpHeaders.Authorization, "Bearer $token2")
            contentType(ContentType.Application.Json)
            setBody("""{"quantity": 5}""")
        }

        assertTrue(
            resp.status == HttpStatusCode.Forbidden || resp.status == HttpStatusCode.NotFound,
            "Expected 403 or 404, but got ${resp.status}"
        )

        val text = resp.bodyAsText()
        assertTrue(
            text.contains("Not your wishlist item") || text.contains("Wishlist not found"),
            "Unexpected response body: $text"
        )
    }

}
