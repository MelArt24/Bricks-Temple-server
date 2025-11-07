package com.brickstemple.dto.wishlist

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class WishlistItemDto(
    val id: Int? = null,
    val wishlistId: Int,
    val productId: Int,
    val quantity: Int,
    @Contextual val addedAt: LocalDateTime? = null
)