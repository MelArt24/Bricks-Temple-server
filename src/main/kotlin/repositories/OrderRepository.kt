package com.brickstemple.repositories

import com.brickstemple.dto.orders.OrderDto
import com.brickstemple.models.OrderStatus
import com.brickstemple.models.Orders
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

open class OrderRepository {

    open fun getAll(): List<OrderDto> = transaction {
        Orders.selectAll().map {
            OrderDto(
                id = it[Orders.id],
                userId = it[Orders.userId],
                status = OrderStatus.valueOf(it[Orders.status].uppercase()),
                totalPrice = it[Orders.totalPrice],
                createdAt = it[Orders.createdAt]
            )
        }
    }

    open fun getById(id: Int): OrderDto? = transaction {
        Orders.select { Orders.id eq id }.singleOrNull()?.let {
            OrderDto(
                id = it[Orders.id],
                userId = it[Orders.userId],
                status = OrderStatus.valueOf(it[Orders.status].uppercase()),
                totalPrice = it[Orders.totalPrice],
                createdAt = it[Orders.createdAt]
            )
        }
    }

    open fun create(order: OrderDto): Int = transaction {
        Orders.insert {
            it[userId] = order.userId
            it[status] = order.status.value
            it[totalPrice] = order.totalPrice
            it[createdAt] = LocalDateTime.now()
        } get Orders.id
    }

    open fun updateStatus(id: Int, status: OrderStatus): Boolean = transaction {
        Orders.update({ Orders.id eq id }) {
            it[Orders.status] = status.value
        } > 0
    }

    open fun delete(id: Int): Boolean = transaction {
        Orders.deleteWhere { Orders.id eq id } > 0
    }
}
