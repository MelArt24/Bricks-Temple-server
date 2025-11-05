package com.brickstemple.dto.orders

import CreateOrderItemRequest
import kotlinx.serialization.Serializable

@Serializable
data class CreateOrderRequest(
    val items: List<CreateOrderItemRequest>,
    val totalPrice: Double
)
