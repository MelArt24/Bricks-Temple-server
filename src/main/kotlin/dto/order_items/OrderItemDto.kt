package com.brickstemple.dto.order_items

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class OrderItemDto(
    val id: Int? = null,
    val orderId: Int,
    val productId: Int,
    val quantity: Int,
    @Contextual val priceAtPurchase: BigDecimal
)
