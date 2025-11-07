package com.brickstemple.fakeRepositories

import com.brickstemple.dto.wishlist.WishlistItemDto
import com.brickstemple.repositories.WishlistItemRepository
import java.time.LocalDateTime

class FakeWishlistItemRepository : WishlistItemRepository() {
    private val items = mutableListOf<WishlistItemDto>()
    private var idCounter = 1

    override fun addOrIncrement(wishlistId: Int, productId: Int) {
        val existing = items.firstOrNull { it.wishlistId == wishlistId && it.productId == productId }
        if (existing != null) {
            val updated = existing.copy(quantity = existing.quantity + 1)
            items[items.indexOf(existing)] = updated
        } else {
            items.add(
                WishlistItemDto(
                    id = idCounter++,
                    wishlistId = wishlistId,
                    productId = productId,
                    quantity = 1,
                    addedAt = LocalDateTime.now()
                )
            )
        }
    }

    override fun getById(id: Int): WishlistItemDto? = items.firstOrNull { it.id == id }

    override fun getByWishlist(wishlistId: Int): List<WishlistItemDto> =
        items.filter { it.wishlistId == wishlistId }

    override fun delete(id: Int): Boolean = items.removeIf { it.id == id }

    override fun clearWishlist(wishlistId: Int): Boolean =
        items.removeIf { it.wishlistId == wishlistId }

    override fun updateQuantity(itemId: Int, quantity: Int): Boolean {
        val existing = getById(itemId) ?: return false
        items[items.indexOf(existing)] = existing.copy(quantity = quantity)
        return true
    }

    override fun decrementOrDelete(itemId: Int): Boolean {
        val existing = getById(itemId) ?: return false
        return if (existing.quantity > 1) {
            updateQuantity(itemId, existing.quantity - 1)
        } else {
            delete(itemId)
        }
    }
}
