package com.brickstemple.repositories

import com.brickstemple.dto.wishlist.WishlistItemDto
import com.brickstemple.models.WishlistItems
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

open class WishlistItemRepository {
    open fun addOrIncrement(wishlistId: Int, productId: Int) = transaction {
        val existing = WishlistItems.select {
            (WishlistItems.wishlistId eq wishlistId) and
                    (WishlistItems.productId eq productId)
        }.singleOrNull()

        if (existing != null) {
            WishlistItems.update({ WishlistItems.id eq existing[WishlistItems.id] }) {
                it[quantity] = existing[WishlistItems.quantity] + 1
            }
        } else {
            WishlistItems.insert {
                it[WishlistItems.wishlistId] = wishlistId
                it[WishlistItems.productId] = productId
                it[quantity] = 1
                it[addedAt] = LocalDateTime.now()
            }
        }
    }

    open fun getById(id: Int): WishlistItemDto? = transaction {
        WishlistItems.select { WishlistItems.id eq id }
            .singleOrNull()
            ?.let {
                WishlistItemDto(
                    id = it[WishlistItems.id],
                    wishlistId = it[WishlistItems.wishlistId],
                    productId = it[WishlistItems.productId],
                    quantity = it[WishlistItems.quantity],
                    addedAt = it[WishlistItems.addedAt]
                )
            }
    }

    open fun delete(id: Int): Boolean = transaction {
        WishlistItems.deleteWhere { WishlistItems.id eq id } > 0
    }

    open fun getByWishlist(wishlistId: Int): List<WishlistItemDto> = transaction {
        WishlistItems.select { WishlistItems.wishlistId eq wishlistId }
            .map {
                WishlistItemDto(
                    id = it[WishlistItems.id],
                    wishlistId = it[WishlistItems.wishlistId],
                    productId = it[WishlistItems.productId],
                    quantity = it[WishlistItems.quantity],
                    addedAt = it[WishlistItems.addedAt]
                )
            }
    }

    open fun clearWishlist(wishlistId: Int): Boolean = transaction {
        WishlistItems.deleteWhere { WishlistItems.wishlistId eq wishlistId } > 0
    }

    open fun updateQuantity(itemId: Int, quantity: Int): Boolean = transaction {
        WishlistItems.update({ WishlistItems.id eq itemId }) {
            it[WishlistItems.quantity] = quantity
        } > 0
    }

    open fun decrementOrDelete(itemId: Int): Boolean = transaction {
        val item = WishlistItems.select { WishlistItems.id eq itemId }.singleOrNull()
            ?: return@transaction false

        val currentQty = item[WishlistItems.quantity]

        if (currentQty > 1) {
            WishlistItems.update({ WishlistItems.id eq itemId }) {
                it[quantity] = currentQty - 1
            }
        } else {
            WishlistItems.deleteWhere { WishlistItems.id eq itemId }
        }
        true
    }

}
