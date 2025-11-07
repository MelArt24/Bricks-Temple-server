package com.brickstemple

import com.brickstemple.plugins.configureRateLimiting
import com.brickstemple.plugins.configureSecurity
import com.brickstemple.plugins.configureSerialization
import com.brickstemple.repositories.*
import com.brickstemple.routes.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.auth.*


fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module(
    testing: Boolean = false,
    userRepo: UserRepository = UserRepository(),
    productRepo: ProductRepository = ProductRepository(),
) {
    if (!testing) {
        configureDatabase()
    }

    configureSerialization()
    configureSecurity()
    configureRateLimiting()

    routing {
        authRoutes(userRepo)

        authenticate("auth-jwt") {
            userRoutes(userRepo)
            orderRoutes(OrderRepository(), OrderItemRepository(), ProductRepository())
            wishlistRoutes(WishlistRepository(), WishlistItemRepository())
        }

        productRoutes(productRepo)

        get("/health") { call.respondText("OK") }
    }
}
