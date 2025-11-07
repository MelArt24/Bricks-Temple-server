package com.brickstemple.fakeRepositories

import com.brickstemple.dto.wishlist.WishlistDto
import com.brickstemple.repositories.WishlistRepository
import java.time.LocalDateTime

class FakeWishlistRepository : WishlistRepository() {
    private val wishlists = mutableListOf<WishlistDto>()
    private var idCounter = 1

    override fun create(userId: Int): Int {
        val existing = wishlists.firstOrNull { it.userId == userId }
        if (existing != null) return existing.id

        val id = idCounter++
        wishlists.add(WishlistDto(id, userId, LocalDateTime.now()))
        return id
    }

    override fun getByUser(userId: Int): WishlistDto? {
        return wishlists.firstOrNull { it.userId == userId }
    }

    fun clear() {
        wishlists.clear()
        idCounter = 1
    }
}
