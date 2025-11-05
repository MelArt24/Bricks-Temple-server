package com.brickstemple.routes

import com.brickstemple.dto.CreateOrderRequest
import com.brickstemple.dto.CreatedResponse
import com.brickstemple.dto.ErrorResponse
import com.brickstemple.dto.OrderDto
import com.brickstemple.models.OrderStatus
import com.brickstemple.repositories.OrderRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.orderRoutes(repo: OrderRepository) {

    route("/orders") {

        authenticate("auth-jwt") {
            post {
                try {
                    val principal = call.principal<JWTPrincipal>()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse("No token"))

                    val userId = principal.payload.getClaim("id").asInt()

                    val body = call.receive<CreateOrderRequest>()

                    val newOrderId = repo.create(
                        OrderDto(
                            userId = userId,
                            totalPrice = body.totalPrice.toBigDecimal(),
                            status = OrderStatus.PENDING
                        )
                    )

                    call.respond(
                        HttpStatusCode.Created,
                        CreatedResponse("Order created", newOrderId)
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Server error", e.message)
                    )
                }
            }
        }

        authenticate("auth-jwt") {
            get {
                val role = call.principal<JWTPrincipal>()!!.payload.getClaim("role").asString()
                if (role != "admin") {
                    return@get call.respond(HttpStatusCode.Forbidden, ErrorResponse("Admins only"))
                }

                val orders = repo.getAll()
                call.respond(HttpStatusCode.OK, orders)
            }
        }

        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asInt()

                val orders = repo.getAll().filter { it.userId == userId }
                call.respond(HttpStatusCode.OK, orders)
            }
        }

        authenticate("auth-jwt") {
            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid id"))

                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asInt()
                val role = principal.payload.getClaim("role").asString()

                val order = repo.getById(id)
                    ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("Order not found"))

                if (order.userId != userId && role != "admin") {
                    return@get call.respond(HttpStatusCode.Forbidden, ErrorResponse("Not your order"))
                }

                call.respond(HttpStatusCode.OK, order)
            }
        }

        authenticate("auth-jwt") {
            put("/{id}/status") {
                val principal = call.principal<JWTPrincipal>()!!
                val role = principal.payload.getClaim("role").asString()

                if (role != "admin") {
                    return@put call.respond(HttpStatusCode.Forbidden, ErrorResponse("Admins only"))
                }

                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid id"))

                val body = call.receive<Map<String, String>>()
                val statusString = body["status"] ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing status"))

                val newStatus = try {
                    OrderStatus.valueOf(statusString.uppercase())
                } catch (e: Exception) {
                    return@put call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Invalid status. Use: PENDING, CONFIRMED, DELIVERED, CANCELLED")
                    )
                }

                val updated = repo.updateStatus(id, newStatus)
                if (updated) call.respond(HttpStatusCode.OK, mapOf("message" to "Status updated"))
                else call.respond(HttpStatusCode.NotFound, ErrorResponse("Order not found"))
            }
        }
    }
}
