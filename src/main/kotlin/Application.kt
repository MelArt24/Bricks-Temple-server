package com.brickstemple

import com.brickstemple.repositories.ProductRepository
import com.brickstemple.plugins.configureSerialization
import com.brickstemple.repositories.UserRepository
import com.brickstemple.routes.productRoutes
import com.brickstemple.routes.userRoutes
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module(testing: Boolean = false) {

    if (!testing) {
        configureDatabase()
    }

    configureSerialization()

    routing {
        productRoutes(ProductRepository())
        userRoutes(UserRepository())

        get("/health") { call.respondText("OK") }
    }
}
