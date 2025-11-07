package com.brickstemple.repositories

import com.brickstemple.dto.wishlist.WishlistDto
import com.brickstemple.models.Wishlists
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

open class WishlistRepository {
    open fun create(userId: Int): Int = transaction {
        Wishlists.insert {
            it[Wishlists.userId] = userId
            it[createdAt] = LocalDateTime.now()
        } get Wishlists.id
    }

    open fun getByUser(userId: Int): WishlistDto? = transaction {
        Wishlists.select { Wishlists.userId eq userId }.singleOrNull()?.let {
            WishlistDto(
                id = it[Wishlists.id],
                userId = it[Wishlists.userId],
                createdAt = it[Wishlists.createdAt]
            )
        }
    }
}
