package com.brickstemple.dto.orders

import kotlinx.serialization.Serializable

@Serializable
data class CreateOrderRequest(
    val totalPrice: Double
)
