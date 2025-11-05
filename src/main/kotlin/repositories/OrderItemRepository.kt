package com.brickstemple.repositories

import com.brickstemple.dto.order_items.OrderItemDto
import com.brickstemple.models.OrderItems
import com.brickstemple.models.Orders
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

open class OrderItemRepository {

    open fun getByOrder(orderId: Int): List<OrderItemDto> = transaction {
        OrderItems.select { OrderItems.orderId eq orderId }.map {
            OrderItemDto(
                id = it[OrderItems.id],
                orderId = it[OrderItems.orderId],
                productId = it[OrderItems.productId],
                quantity = it[OrderItems.quantity],
                priceAtPurchase = it[OrderItems.priceAtPurchase]
            )
        }
    }

    open fun create(item: OrderItemDto): Int = transaction {
        OrderItems.insert {
            it[orderId] = item.orderId
            it[productId] = item.productId
            it[quantity] = item.quantity
            it[priceAtPurchase] = item.priceAtPurchase
        } get OrderItems.id
    }

    open fun deleteByOrder(orderId: Int): Int = transaction {
        OrderItems.deleteWhere { OrderItems.orderId eq orderId }
    }
}
