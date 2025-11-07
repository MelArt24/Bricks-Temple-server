package com.brickstemple.dto.order_items

import com.brickstemple.dto.orders.OrderDto
import kotlinx.serialization.Serializable

@Serializable
data class OrderWithItemsResponse(
    val order: OrderDto,
    val items: List<OrderItemDto>
)
