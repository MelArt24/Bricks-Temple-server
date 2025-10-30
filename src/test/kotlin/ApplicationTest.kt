package com.brickstemple

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {
    @Test
    fun testRootEndpoint() = testApplication {
        application {
            module(testing = true)
        }
        val response = client.get("/health")
        assertEquals(200, response.status.value)
        assertEquals("OK", response.bodyAsText())
    }
}
