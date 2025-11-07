package com.brickstemple.routes

import com.brickstemple.dto.order_items.OrderWithItemsResponse
import com.brickstemple.dto.orders.CreateOrderRequest
import com.brickstemple.dto.CreatedResponse
import com.brickstemple.dto.ErrorResponse
import com.brickstemple.dto.order_items.OrderItemDto
import com.brickstemple.dto.orders.OrderDto
import com.brickstemple.models.OrderStatus
import com.brickstemple.repositories.OrderItemRepository
import com.brickstemple.repositories.OrderRepository
import com.brickstemple.repositories.ProductRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.orderRoutes(
    orderRepo: OrderRepository,
    orderItemRepo: OrderItemRepository,
    productRepo: ProductRepository
) {

    route("/orders") {

        authenticate("auth-jwt") {
            post {
                try {
                    val principal = call.principal<JWTPrincipal>()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse("No token"))

                    val userId = principal.payload.getClaim("id").asInt()

                    val body = try {
                        call.receive<CreateOrderRequest>()
                    } catch (e: Exception) {
                        return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request", e.message))
                    }

                    if (body.items.isEmpty()) {
                        return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Order must contain at least 1 item"))
                    }

                    var realTotal = 0.toBigDecimal()

                    for (item in body.items) {
                        val product = productRepo.getById(item.productId)
                            ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Product ${item.productId} not found"))

                        val subtotal = product.price * item.quantity.toBigDecimal()
                        realTotal += subtotal
                    }

                    if (realTotal.compareTo(body.totalPrice.toBigDecimal()) != 0) {
                        return@post call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse(
                                "Total price mismatch",
                                "Expected $realTotal, but got ${body.totalPrice}"
                            )
                        )
                    }

                    val newOrderId = orderRepo.create(
                        OrderDto(
                            userId = userId,
                            totalPrice = body.totalPrice.toBigDecimal(),
                            status = OrderStatus.PENDING
                        )
                    )

                    for (item in body.items) {
                        val product = productRepo.getById(item.productId)!!

                        orderItemRepo.create(
                            OrderItemDto(
                                orderId = newOrderId,
                                productId = item.productId,
                                quantity = item.quantity,
                                priceAtPurchase = product.price
                            )
                        )
                    }

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
                call.respond(HttpStatusCode.OK, orderRepo.getAll())
            }
        }

        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asInt()

                val myOrders = orderRepo.getAll().filter { it.userId == userId }

                if (myOrders.isEmpty()) {
                    return@get call.respond(
                        HttpStatusCode.OK,
                        ErrorResponse("No orders yet")
                    )
                }

                call.respond(HttpStatusCode.OK, myOrders )
            }
        }

        authenticate("auth-jwt") {
            get("/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid id"))

                    val principal = call.principal<JWTPrincipal>()!!
                    val userId = principal.payload.getClaim("id").asInt()
                    val role = principal.payload.getClaim("role").asString()

                    val order = orderRepo.getById(id)
                        ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("Order not found"))

                    if (order.userId != userId && role != "admin") {
                        return@get call.respond(HttpStatusCode.Forbidden, ErrorResponse("Not your order"))
                    }

                    val items = orderItemRepo.getByOrder(id)

                    call.respond(
                        HttpStatusCode.OK,
                        OrderWithItemsResponse(order, items)
                    )
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Server error", e.message))
                }
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

                val updated = orderRepo.updateStatus(id, newStatus)
                if (updated) call.respond(HttpStatusCode.OK, mapOf("message" to "Status updated"))
                else call.respond(HttpStatusCode.NotFound, ErrorResponse("Order not found"))
            }
        }
    }
}
