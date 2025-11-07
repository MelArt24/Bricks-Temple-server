package com.brickstemple.routes

import WishlistResponse
import com.brickstemple.dto.ErrorResponse
import com.brickstemple.dto.wishlist.WishlistDto
import com.brickstemple.repositories.WishlistItemRepository
import com.brickstemple.repositories.WishlistRepository
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.time.LocalDateTime

fun Route.wishlistRoutes(
    wishlistRepo: WishlistRepository,
    wishlistItemRepo: WishlistItemRepository
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
        }
    }
}
