package com.brickstemple.fakeRepositories

import com.brickstemple.dto.OrderDto
import com.brickstemple.models.OrderStatus
import com.brickstemple.repositories.OrderRepository
import java.time.LocalDateTime

class FakeOrderRepository : OrderRepository() {

    private val orders = mutableListOf<OrderDto>()
    private var idCounter = 1

    override fun getAll(): List<OrderDto> = orders.toList()

    override fun getById(id: Int): OrderDto? = orders.find { it.id == id }

    override fun create(order: OrderDto): Int {
        val created = order.copy(
            id = idCounter++,
            status = order.status,
            createdAt = order.createdAt ?: LocalDateTime.now()
        )
        orders.add(created)
        return created.id!!
    }

    override fun updateStatus(id: Int, status: OrderStatus): Boolean {
        val idx = orders.indexOfFirst { it.id == id }
        if (idx == -1) return false
        val current = orders[idx]
        orders[idx] = current.copy(status = status)
        return true
    }

    override fun delete(id: Int): Boolean = orders.removeIf { it.id == id }
}
