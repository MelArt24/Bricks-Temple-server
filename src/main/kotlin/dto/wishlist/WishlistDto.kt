package com.brickstemple.dto.wishlist

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class WishlistDto(
    val id: Int,
    val userId: Int,
    @Contextual val createdAt: LocalDateTime
)
