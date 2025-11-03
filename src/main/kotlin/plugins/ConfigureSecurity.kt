package com.brickstemple.plugins

import com.brickstemple.security.JwtConfig
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = JwtConfig.realm

            verifier(JwtConfig.verifier)

            validate { credential ->
                val email = credential.payload.getClaim("email").asString()
                val id = credential.payload.getClaim("id").asInt()

                if (email.isNotBlank() && id != null) {
                    JWTPrincipal(credential.payload)
                } else null
            }
        }
    }
}
