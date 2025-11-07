package integrationTests

import com.brickstemple.dto.users.UserDto
import com.brickstemple.fakeRepositories.FakeProductRepository
import com.brickstemple.fakeRepositories.FakeUserRepository
import com.brickstemple.plugins.configureSecurity
import com.brickstemple.plugins.configureSerialization
import com.brickstemple.routes.productRoutes
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import io.ktor.server.routing.*
import com.brickstemple.routes.authRoutes
import io.ktor.client.*


class ProductRoutesErrorTest {

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
    fun `GET non-existent product returns 404`() = testApplication {
        application {
            configureSerialization()
            configureSecurity()
            routing { productRoutes(FakeProductRepository()) }
        }

        val response = client.get("/products/999")
        assertEquals(HttpStatusCode.NotFound, response.status, response.bodyAsText())
    }

    @Test
    fun `GET invalid id returns 400`() = testApplication {
        application {
            configureSerialization()
            configureSecurity()
            routing { productRoutes(FakeProductRepository()) }
        }

        val response = client.get("/products/abc")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Invalid"))
    }

    @Test
    fun `DELETE non-existent product returns 404`() = testApplication {
        val userRepo = FakeUserRepository()
        val productRepo = FakeProductRepository()

        userRepo.create(UserDto(username = "admin", email = "admin@mail.com", password = "123456", role = "admin"))

        application {
            configureSerialization()
            configureSecurity()
            routing {
                authRoutes(userRepo)
                productRoutes(productRepo)
            }
        }

        val token = login(client, "admin@mail.com", "123456")

        val response = client.delete("/products/12345") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

}
