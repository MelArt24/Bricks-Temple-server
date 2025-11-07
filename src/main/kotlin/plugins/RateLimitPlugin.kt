package com.brickstemple.plugins

import com.brickstemple.dto.ErrorResponse
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.plugins.*
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

fun Application.configureRateLimiting() {

    val requestsMap = ConcurrentHashMap<String, MutableList<Long>>()
    val limit = 100
    val windowMs = 60_000L

    intercept(ApplicationCallPipeline.Plugins) {
        val ip = call.request.origin.remoteHost ?: "unknown"

        val now = Instant.now().toEpochMilli()
        val timestamps = requestsMap.getOrPut(ip) { mutableListOf() }

        timestamps.removeIf { it < now - windowMs }

        if (timestamps.size >= limit) {
            call.respond(
                HttpStatusCode.TooManyRequests,
                ErrorResponse("error", "Too many requests. Limit is $limit per minute")
            )
            finish()
        } else {
            timestamps.add(now)
        }
    }
}
