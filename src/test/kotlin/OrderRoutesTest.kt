package com.brickstemple

import TokenResponse
import com.brickstemple.dto.orders.OrderDto
import com.brickstemple.dto.products.ProductDto
import com.brickstemple.dto.users.UserDto
import com.brickstemple.fakeRepositories.FakeOrderItemRepository
import com.brickstemple.fakeRepositories.FakeOrderRepository
import com.brickstemple.fakeRepositories.FakeProductRepository
import com.brickstemple.fakeRepositories.FakeUserRepository
import com.brickstemple.models.OrderStatus
import com.brickstemple.plugins.configureSecurity
import com.brickstemple.plugins.configureSerialization
import com.brickstemple.routes.authRoutes
import com.brickstemple.routes.orderRoutes
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import io.ktor.server.routing.*
import io.ktor.client.*
import java.math.BigDecimal

class OrderRoutesTest {

    private suspend fun login(client: HttpClient, email: String, password: String): String {
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
    fun `POST orders - user can create order with items`() = testApplication {
        val userRepo = FakeUserRepository()
        val orderRepo = FakeOrderRepository()
        val orderItemsRepo = FakeOrderItemRepository()
        val productRepo = FakeProductRepository()

        userRepo.create(UserDto(username = "test", email = "test@mail.com", password = "123456"))

        val productId = productRepo.create(
            ProductDto(
                name = "Test Product",
                category = "test-category",
                number = 1,
                condition = "new",
                price = BigDecimal("50.00"),
                type = "lego"
            )
        )

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(userRepo)
                orderRoutes(orderRepo, orderItemsRepo, productRepo)
            }
        }

        val token = login(client, "test@mail.com", "123456")

        val response = client.post("/orders") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "totalPrice": 100.0,
                    "items": [
                        { "productId": 1, "quantity": 2, "priceAtPurchase": 50.0 }
                    ]
                }
            """.trimIndent())
        }

        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(response.bodyAsText().contains("Order created"))
    }

    @Test
    fun `POST orders - no token returns 401`() = testApplication {
        val orderRepo = FakeOrderRepository()
        val itemsRepo = FakeOrderItemRepository()
        val productRepo = FakeProductRepository()

        application {
            configureSerialization()
            configureSecurity()
            routing { orderRoutes(orderRepo, itemsRepo, productRepo) }
        }

        val response = client.post("/orders") {
            contentType(ContentType.Application.Json)
            setBody("""{
                "totalPrice": 150.0,
                "items": [ { "productId": 2, "quantity": 1, "priceAtPurchase": 150.0 } ]
            }""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET orders - only admin can view all`() = testApplication {
        val userRepo = FakeUserRepository()
        val orderRepo = FakeOrderRepository()
        val itemsRepo = FakeOrderItemRepository()
        val productRepo = FakeProductRepository()

        val adminId = userRepo.create(
            UserDto(username = "admin", email = "admin@mail.com", password = "123456", role = "admin")
        )
        userRepo.create(
            UserDto(username = "user", email = "user@mail.com", password = "123456", role = "user")
        )

        orderRepo.create(
            OrderDto(userId = adminId, totalPrice = BigDecimal("100.0"), status = OrderStatus.PENDING)
        )
        orderRepo.create(
            OrderDto(userId = adminId, totalPrice = BigDecimal("200.0"), status = OrderStatus.PENDING)
        )

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(userRepo)
                orderRoutes(orderRepo, itemsRepo, productRepo)
            }
        }

        val adminToken = login(client, "admin@mail.com", "123456")
        val userToken = login(client, "user@mail.com", "123456")

        val adminResponse = client.get("/orders") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
        }
        assertEquals(HttpStatusCode.OK, adminResponse.status)

        val body = adminResponse.bodyAsText()

        assertTrue(body.contains("100"))
        assertTrue(body.contains("200"))

        val userResponse = client.get("/orders") {
            header(HttpHeaders.Authorization, "Bearer $userToken")
        }
        assertEquals(HttpStatusCode.Forbidden, userResponse.status)
    }

    @Test
    fun `GET orders - user sees only own`() = testApplication {
        val users = FakeUserRepository()
        val orders = FakeOrderRepository()
        val items = FakeOrderItemRepository()
        val productRepo = FakeProductRepository()

        val userId = users.create(UserDto(username = "user1", email = "u1@mail.com", password = "123456"))
        val otherId = users.create(UserDto(username ="user2", email = "u2@mail.com", password = "123456"))

        orders.create(
            OrderDto(userId = userId, totalPrice = BigDecimal("100.0"), status = OrderStatus.PENDING)
        )

        orders.create(
            OrderDto(userId = userId, totalPrice = BigDecimal("200.0"), status = OrderStatus.PENDING)
        )

        orders.create(
            OrderDto(userId = otherId, totalPrice = BigDecimal("400.0"), status = OrderStatus.PENDING)
        )

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(users)
                orderRoutes(orders, items, productRepo)
            }
        }

        val token = login(client, "u1@mail.com", "123456")

        val response = client.get("/orders/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("100"))
        assertFalse(body.contains("300"))
    }


    @Test
    fun `GET orders id - user cannot view others`() = testApplication {
        val users = FakeUserRepository()
        val orders = FakeOrderRepository()
        val items = FakeOrderItemRepository()
        val productRepo = FakeProductRepository()

        users.create(UserDto(username = "user1", email = "u1@mail.com", password = "123456"))
        val otherId = users.create(UserDto(username ="user2", email = "u2@mail.com", password = "123456"))

        val orderId = orders.create(
            OrderDto(userId = otherId, totalPrice = BigDecimal("999.0"), status = OrderStatus.PENDING)
        )

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(users)
                orderRoutes(orders, items, productRepo)
            }
        }

        val token = login(client, "u1@mail.com", "123456")
        val response = client.get("/orders/$orderId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `PUT orders status - admin can update`() = testApplication {
        val users = FakeUserRepository()
        val orders = FakeOrderRepository()
        val items = FakeOrderItemRepository()
        val productRepo = FakeProductRepository()

        users.create(UserDto(username = "admin", email = "ad@mail.com", password = "123456", role = "admin"))
        val userId = users.create(UserDto(username = "user", email = "us@mail.com", password = "123456"))

        val orderId = orders.create(
            OrderDto(userId = userId, totalPrice = BigDecimal("999.0"), status = OrderStatus.PENDING)
        )

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(users)
                orderRoutes(orders, items, productRepo)
            }
        }

        val adminToken = login(client, "ad@mail.com", "123456")

        val response = client.put("/orders/$orderId/status") {
            header(HttpHeaders.Authorization, "Bearer $adminToken")
            contentType(ContentType.Application.Json)
            setBody("""{"status": "DELIVERED"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(OrderStatus.DELIVERED, orders.getById(orderId)!!.status)
    }
}
