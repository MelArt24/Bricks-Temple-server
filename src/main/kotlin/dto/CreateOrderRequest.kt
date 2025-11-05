package com.brickstemple.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateOrderRequest(
    val totalPrice: Double
)
