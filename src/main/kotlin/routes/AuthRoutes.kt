package com.brickstemple.routes

import com.brickstemple.dto.*
import com.brickstemple.repositories.UserRepository
import com.brickstemple.security.JwtConfig
import com.brickstemple.util.HashUtil
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.postgresql.util.PSQLException

fun Route.authRoutes(userRepo: UserRepository) {

    route("/auth") {

        post("/register") {
            try {
                val user = call.receive<RegisterRequest>()

                if (user.username.isBlank() || user.email.isBlank() || user.password.isBlank()) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Missing username, email or password")
                    )
                }

                if (!user.email.contains("@")) {
                    return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid email format"))
                }

                if (user.password.length < 6) {
                    return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Password must be at least 6 characters long"))
                }

                val existing = userRepo.getByEmail(user.email)
                if (existing != null) {
                    return@post call.respond(
                        HttpStatusCode.Conflict,
                        ErrorResponse("User with this email already exists")
                    )
                }

                val newUserId = userRepo.create(
                    UserDto(
                        username = user.username,
                        email = user.email,
                        password = user.password
                    )
                )

                call.respond(HttpStatusCode.Created, CreatedResponse("User registered successfully", newUserId))

            } catch (e: PSQLException) {
                call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse("User with this email or username already exists", e.message)
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("Unexpected error", e.message)
                )
            }
        }

        post("/login") {
            try {
                val body = call.receive<LoginRequest>()

                val user = userRepo.getByEmail(body.email)
                    ?: return@post call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("User not found")
                    )

                if (!HashUtil.checkPassword(body.password, user.password)) {
                    return@post call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("Invalid password")
                    )
                }

                val token = JwtConfig.generateToken(
                    user.email,
                    user.id!!,
                    role = user.role)
                call.respond(mapOf("token" to token))

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse("Invalid request", e.message)
                )
            }
        }
    }
}
