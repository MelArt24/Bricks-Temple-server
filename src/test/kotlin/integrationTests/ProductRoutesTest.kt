package integrationTests

import com.brickstemple.dto.products.ProductDto
import com.brickstemple.dto.users.UserDto
import com.brickstemple.fakeRepositories.FakeProductRepository
import com.brickstemple.fakeRepositories.FakeUserRepository
import com.brickstemple.plugins.configureSecurity
import com.brickstemple.plugins.configureSerialization
import com.brickstemple.routes.productRoutes
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.*
import java.math.BigDecimal
import java.time.LocalDateTime
import com.brickstemple.routes.authRoutes

/**
 * Integration tests for Product API endpoints (GET, POST, PUT, DELETE)
 * using an in-memory fake repository instead of a real PostgreSQL database.
 */
class ProductRoutesTest {

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

    private lateinit var fakeRepo: FakeProductRepository
    private lateinit var userRepo: FakeUserRepository

    @BeforeTest
    fun setup() {
        fakeRepo = FakeProductRepository()
        userRepo = FakeUserRepository()
    }

    @Test
    fun testGetAllProducts_emptyList() = testApplication {
        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(userRepo)
                productRoutes(fakeRepo)
            }
        }

        val response = client.get("/products")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("No products"))
    }

    @Test
    fun testCreateProduct_success() = testApplication {

        userRepo.create(UserDto(username = "admin", email = "admin@mail.com", password = "123456", role = "admin"))

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(userRepo)
                productRoutes(fakeRepo)
            }
        }

        val token = login(client, "admin@mail.com", "123456")

        val response = client.post("/products") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "name": "X-Wing",
                    "category": "Star Wars",
                    "number": "75301",
                    "condition": "New",
                    "price": 49.99,
                    "type": "set"
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(response.bodyAsText().contains("Product created successfully"))
    }

    @Test
    fun testGetProductById_found() = testApplication {
        fakeRepo.create(
            ProductDto(
                id = 1,
                name = "Millennium Falcon",
                category = "Star Wars",
                number = "75192",
                details = 7541,
                minifigures = 8,
                age = "18+",
                year = "2023",
                size = "21x84x56",
                condition = "New",
                price = BigDecimal("799.99"),
                createdAt = LocalDateTime.now(),
                image = "https://example.com/falcon.jpg",
                description = "Ultimate Collector Series",
                type = "set",
                keywords = "falcon;star wars;spaceship"
            )
        )

        application {
            configureSerialization()
            configureSecurity()
            routing { productRoutes(fakeRepo) }
        }

        val response = client.get("/products/1")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Millennium Falcon"))
    }

    @Test
    fun testUpdateProduct_success() = testApplication {
        userRepo.create(UserDto(username = "admin", email = "admin2@mail.com", password = "123456", role = "admin"))

        fakeRepo.create(
            ProductDto(
                id = 1,
                name = "TIE Fighter",
                category = "Star Wars",
                number = "75300",
                condition = "New",
                price = BigDecimal("49.99"),
                createdAt = LocalDateTime.now(),
                type = "set"
            )
        )

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(userRepo)
                productRoutes(fakeRepo)
            }
        }

        val token = login(client, "admin2@mail.com", "123456")

        val response = client.put("/products/1") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "name": "TIE Fighter Advanced",
                    "price": 59.99,
                    "condition": "Used"
                }
                """.trimIndent()
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Updated successfully"))
    }

    @Test
    fun testDeleteProduct_success() = testApplication {
        userRepo.create(UserDto(username = "admin", email = "admin3@mail.com", password = "123456", role = "admin"))

        fakeRepo.create(
            ProductDto(
                id = 1,
                name = "Imperial Shuttle",
                category = "Star Wars",
                number = "75302",
                condition = "New",
                price = BigDecimal("89.99"),
                createdAt = LocalDateTime.now(),
                type = "set"
            )
        )

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(userRepo)
                productRoutes(fakeRepo)
            }
        }

        val token = login(client, "admin3@mail.com", "123456")

        val response = client.delete("/products/1") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.NoContent, response.status)
    }
}
