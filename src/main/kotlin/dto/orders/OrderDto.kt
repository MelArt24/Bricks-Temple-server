package com.brickstemple.dto.orders

import com.brickstemple.models.OrderStatus
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
data class OrderDto(
    val id: Int? = null,
    val userId: Int,
    val status: OrderStatus = OrderStatus.PENDING,
    @Contextual val totalPrice: BigDecimal,
    @Contextual val createdAt: LocalDateTime? = null
)
