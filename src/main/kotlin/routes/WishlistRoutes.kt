package com.brickstemple.routes

import WishlistResponse
import com.brickstemple.dto.CreatedResponse
import com.brickstemple.dto.ErrorResponse
import com.brickstemple.dto.order_items.OrderItemDto
import com.brickstemple.dto.orders.OrderDto
import com.brickstemple.dto.wishlist.WishlistDto
import com.brickstemple.models.OrderStatus
import com.brickstemple.repositories.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.math.BigDecimal
import java.time.LocalDateTime

fun Route.wishlistRoutes(
    wishlistRepo: WishlistRepository,
    wishlistItemRepo: WishlistItemRepository,
    orderRepo: OrderRepository,
    orderItemRepo: OrderItemRepository,
    productRepo: ProductRepository
) {
    route("/wishlist") {

        authenticate("auth-jwt") {
            get {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asInt()

                val wishlist = wishlistRepo.getByUser(userId)
                    ?: return@get call.respond(HttpStatusCode.OK, mapOf("message" to "Wishlist is empty"))

                val items = wishlistItemRepo.getByWishlist(wishlist.id)

                if (items.isEmpty()) {
                    return@get call.respond(
                        HttpStatusCode.OK,
                        mapOf("message" to "Wishlist is empty")
                    )
                }

                call.respond(
                    HttpStatusCode.OK,
                    WishlistResponse(wishlist = wishlist, items = items)
                )
            }

            post("/add") {
                val principal = call.principal<JWTPrincipal>()!!
                val userId = principal.payload.getClaim("id").asInt()

                val wishlist = wishlistRepo.getByUser(userId) ?: WishlistDto(wishlistRepo.create(userId), userId, LocalDateTime.now())

                val body = call.receive<Map<String, Int>>()
                val productId = body["productId"] ?: return@post call.respond(HttpStatusCode.BadRequest)

                wishlistItemRepo.addOrIncrement(wishlist.id, productId)

                call.respond(HttpStatusCode.Created, mapOf("message" to "Added to wishlist"))
            }

            delete("/remove/{id}") {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("id").asInt()
                val itemId = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid or missing item ID"))

                val wishlist = wishlistRepo.getByUser(userId)
                    ?: return@delete call.respond(HttpStatusCode.NotFound, mapOf("error" to "Wishlist not found"))

                val item = wishlistItemRepo.getById(itemId)
                    ?: return@delete call.respond(HttpStatusCode.NotFound, mapOf("error" to "Item not found"))

                if (item.wishlistId != wishlist.id) {
                    return@delete call.respond(HttpStatusCode.Forbidden, mapOf("error" to "This item does not belong to your wishlist"))
                }

                wishlistItemRepo.decrementOrDelete(itemId)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Quantity decreased or item deleted"))
            }

            delete("/clear") {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("id").asInt()
                val wishlist = wishlistRepo.getByUser(userId)
                    ?: return@delete call.respond(HttpStatusCode.OK, mapOf("message" to "Wishlist is already empty"))

                wishlistItemRepo.clearWishlist(wishlist.id)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Wishlist cleared"))
            }

            put("/item/{id}") {
                val userId = call.principal<JWTPrincipal>()!!.payload.getClaim("id").asInt()
                val itemId = call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid item ID"))

                val body = call.receive<Map<String, Int>>()
                val newQuantity = body["quantity"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing quantity"))

                if (newQuantity <= 0) {
                    return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Quantity must be > 0"))
                }

                val wishlist = wishlistRepo.getByUser(userId)
                    ?: return@put call.respond(HttpStatusCode.NotFound, ErrorResponse("Wishlist not found"))

                val item = wishlistItemRepo.getById(itemId)
                    ?: return@put call.respond(HttpStatusCode.NotFound, ErrorResponse("Item not found"))

                if (item.wishlistId != wishlist.id) {
                    return@put call.respond(HttpStatusCode.Forbidden, ErrorResponse("Not your wishlist item"))
                }

                wishlistItemRepo.updateQuantity(itemId, newQuantity)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Quantity updated"))
            }

            post("/checkout") {
                val principal = call.principal<JWTPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Not authorized"))

                val userId = principal.payload.getClaim("id").asInt()

                val wishlist = wishlistRepo.getByUser(userId)
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Wishlist not found"))

                val items = wishlistItemRepo.getByWishlist(wishlist.id)
                if (items.isEmpty()) {
                    return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Wishlist is empty"))
                }

                var totalPrice = BigDecimal.ZERO
                for (item in items) {
                    val product = productRepo.getById(item.productId)
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Product ${item.productId} not found"))
                    totalPrice += product.price * item.quantity.toBigDecimal()
                }

                val orderId = orderRepo.create(
                    OrderDto(
                        userId = userId,
                        status = OrderStatus.PENDING,
                        totalPrice = totalPrice
                    )
                )

                for (item in items) {
                    val product = productRepo.getById(item.productId)!!
                    orderItemRepo.create(
                        OrderItemDto(
                            orderId = orderId,
                            productId = item.productId,
                            quantity = item.quantity,
                            priceAtPurchase = product.price
                        )
                    )
                }

                wishlistItemRepo.clearWishlist(wishlist.id)

                call.respond(
                    HttpStatusCode.Created,
                    CreatedResponse("Wishlist converted to order", orderId)
                )
            }
        }
    }
}
