package integrationTests

import com.brickstemple.plugins.configureRateLimiting
import com.brickstemple.plugins.configureSerialization
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.testing.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import kotlin.test.*

class RateLimitingTest {

    @Test
    fun `should return 429 after 100 requests from the same IP`() = testApplication {
        application {
            configureSerialization()
            configureRateLimiting()
            routing {
                get("/test") {
                    call.respond(HttpStatusCode.OK, "OK")
                }
            }
        }

        repeat(100) {
            val response = client.get("/test") {
                header(HttpHeaders.Accept, "application/json")
            }
            assertEquals(HttpStatusCode.OK, response.status)
        }

        val blocked = client.get("/test") {
            header(HttpHeaders.Accept, "application/json")
        }

        assertEquals(HttpStatusCode.TooManyRequests, blocked.status)
        assertTrue(blocked.bodyAsText().contains("Too many requests"))
    }
}
