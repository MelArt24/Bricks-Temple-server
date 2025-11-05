package com.brickstemple.fakeRepositories

import com.brickstemple.dto.order_items.OrderItemDto
import com.brickstemple.repositories.OrderItemRepository
import java.util.concurrent.atomic.AtomicInteger

class FakeOrderItemRepository : OrderItemRepository() {

    private val items = mutableListOf<OrderItemDto>()
    private val idCounter = AtomicInteger(1)

    fun getAll(): List<OrderItemDto> = items.toList()

    override fun getByOrder(orderId: Int): List<OrderItemDto> =
        items.filter { it.orderId == orderId }

    override fun create(item: OrderItemDto): Int {
        val newItem = item.copy(id = idCounter.getAndIncrement())
        items.add(newItem)
        return newItem.id!!
    }

    override fun deleteByOrder(orderId: Int): Int {
        val before = items.size
        items.removeIf { it.orderId == orderId }
        return before - items.size
    }
}
