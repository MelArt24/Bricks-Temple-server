package com.brickstemple

import com.brickstemple.fakeRepositories.FakeProductRepository
import com.brickstemple.plugins.configureSerialization
import com.brickstemple.routes.productRoutes
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import io.ktor.server.routing.*

class ProductRoutesErrorTest {

    @Test
    fun `GET non-existent product returns 404`() = testApplication {
        application {
            configureSerialization()
            routing { productRoutes(FakeProductRepository()) }
        }

        val response = client.get("/products/999")
        assertEquals(HttpStatusCode.NotFound, response.status, response.bodyAsText())
    }

    @Test
    fun `GET invalid id returns 400`() = testApplication {
        application {
            configureSerialization()
            routing { productRoutes(FakeProductRepository()) }
        }

        val response = client.get("/products/abc")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Invalid"))
    }

    @Test
    fun `DELETE non-existent product returns 404`() = testApplication {
        application {
            configureSerialization()
            routing { productRoutes(FakeProductRepository()) }
        }

        val response = client.delete("/products/12345")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
