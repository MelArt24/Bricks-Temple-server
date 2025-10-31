package com.brickstemple.routes

import com.brickstemple.dto.CreatedResponse
import com.brickstemple.dto.UserDto
import com.brickstemple.repositories.UserRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.postgresql.util.PSQLException


fun Route.userRoutes(repo: UserRepository) {

    route("/users") {

        get {
            try {
                val users = repo.getAll()
                if (users.isEmpty()) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "No users found"))
                } else {
                    call.respond(HttpStatusCode.OK, users)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Unexpected error: ${e.message}"))
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

        post {
            try {
                val body = call.receive<UserDto>()

                if (body.username.isBlank() || body.email.isBlank() || body.password.isBlank()) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Missing required fields: username/email/password")
                    )
                }

                if (!body.email.contains("@")) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid email format")
                    )
                }

                val newId = repo.create(body)

                call.respond(HttpStatusCode.Created, CreatedResponse(
                    message = "User created successfully",
                    id = newId
                )
                )

            } catch (e: PSQLException) {
                if (e.message?.contains("unique") == true) {
                    call.respond(HttpStatusCode.Conflict, ErrorResponse("User with this email or username already exists"))
                } else {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Database error: ${e.message}"))
                }

            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid data: ${e.message}"))
            }
        }


        put("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid id"))

            try {
                val body = call.receive<UserDto>()
                val updated = repo.update(id, body)
                if (updated) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "User updated successfully"))
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("User with id=$id not found"))
                }

            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid data: ${e.message}"))
            }
        }

        delete("{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid id"))

            try {
                val ok = repo.delete(id)
                if (ok) call.respond(HttpStatusCode.NoContent)
                else call.respond(HttpStatusCode.NotFound, ErrorResponse("User with id=$id not found"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Unexpected error: ${e.message}"))
            }
        }
    }
}
