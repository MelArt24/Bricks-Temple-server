package com.brickstemple.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtConfig {
    private const val secret = "super_secret_key"          // можеш винести в .env
    private const val issuer = "bricks-temple"
    private const val audience = "bricks-temple-users"
    const val realm = "bricks-temple"

    val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun generateToken(email: String, id: Int, role: String): String =
        JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("email", email)
            .withClaim("id", id)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 год
            .sign(algorithm)

}
