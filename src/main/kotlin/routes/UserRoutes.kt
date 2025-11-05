package com.brickstemple.routes

import com.brickstemple.dto.ErrorResponse
import com.brickstemple.dto.users.UserDto
import com.brickstemple.dto.users.UserResponseDto
import com.brickstemple.repositories.UserRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import kotlinx.serialization.Serializable

@Serializable
data class UserMeResponse(
    val id: Int,
    val email: String,
    val message: String = "Authenticated successfully"
)


fun Route.userRoutes(repo: UserRepository) {

    route("/users") {

        get {
            try {
                val users = repo.getAll()

                if (users.isEmpty()) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "No users found"))
                } else {
                    val response = users.map {
                        UserResponseDto(
                            id = it.id!!,
                            username = it.username,
                            email = it.email,
                            role = it.role,
                            createdAt = it.createdAt
                        )
                    }
                    call.respond(HttpStatusCode.OK, response)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Unexpected error", e.message))
            }
        }

        get("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid or missing ID"))

            try {
                val user = repo.getById(id)

                if (user == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("User with id=$id not found"))
                } else {
                    call.respond(HttpStatusCode.OK, user)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Unexpected error: ${e.message}"))
            }
        }

        authenticate("auth-jwt") {
            get("/me") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorResponse("No token"))

                    val id = principal.payload.getClaim("id").asInt()
                    val email = principal.payload.getClaim("email").asString()

                    call.respond(
                        HttpStatusCode.OK,
                        UserMeResponse(id = id, email = email)
                    )

                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Server error", e.message)
                    )
                }
            }

            delete("{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val role = principal.payload.getClaim("role").asString()

                if (role != "admin") {
                    return@delete call.respond(HttpStatusCode.Forbidden, ErrorResponse("Only admin can delete users"))
                }

                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid id"))

                try {
                    val ok = repo.delete(id)
                    if (ok) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("User with id=$id not found"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Unexpected error", e.message))
                }
            }

            put("{id}") {
                val principal = call.principal<JWTPrincipal>()!!
                val requesterId = principal.payload.getClaim("id").asInt()
                val requesterRole = principal.payload.getClaim("role").asString()

                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid id"))

                if (requesterRole != "admin" && requesterId != id) {
                    return@put call.respond(HttpStatusCode.Forbidden, ErrorResponse("You can only update your own account"))
                }

                try {
                    val body = call.receive<UserDto>()

                    val safeUserDto = if (requesterRole != "admin") {
                        body.copy(role = "user")
                    } else {
                        body
                    }

                    val updated = repo.update(id, safeUserDto)

                    if (updated) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "User updated successfully"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("User with id=$id not found"))
                    }

                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid data: ${e.message}"))
                }
            }
        }
    }
}
