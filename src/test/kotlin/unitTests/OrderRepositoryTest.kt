package unitTests

import com.brickstemple.dto.orders.OrderDto
import com.brickstemple.fakeRepositories.FakeOrderRepository
import com.brickstemple.models.OrderStatus
import kotlin.test.*
import java.math.BigDecimal

class OrderRepositoryTest {

    @Test
    fun `create - should add order and return id`() {
        val repo = FakeOrderRepository()
        val orderId = repo.create(
            OrderDto(
                userId = 1,
                totalPrice = BigDecimal("100.00"),
                status = OrderStatus.PENDING
            )
        )
        assertEquals(1, orderId)
        assertNotNull(repo.getById(orderId))
    }

    @Test
    fun `getById - returns correct order`() {
        val repo = FakeOrderRepository()
        val id = repo.create(OrderDto(userId = 10, totalPrice = BigDecimal("250.00")))
        val order = repo.getById(id)
        assertNotNull(order)
        assertEquals(10, order.userId)
    }

    @Test
    fun `getAll - returns all orders`() {
        val repo = FakeOrderRepository()
        repo.create(OrderDto(userId = 1, totalPrice = BigDecimal("50.00")))
        repo.create(OrderDto(userId = 2, totalPrice = BigDecimal("150.00")))
        val all = repo.getAll()
        assertEquals(2, all.size)
    }

    @Test
    fun `updateStatus - changes status for existing order`() {
        val repo = FakeOrderRepository()
        val id = repo.create(OrderDto(userId = 1, totalPrice = BigDecimal("120.00")))
        val result = repo.updateStatus(id, OrderStatus.DELIVERED)
        val order = repo.getById(id)

        assertTrue(result)
        assertEquals(OrderStatus.DELIVERED, order!!.status)
    }

    @Test
    fun `delete - removes order`() {
        val repo = FakeOrderRepository()
        val id = repo.create(OrderDto(userId = 1, totalPrice = BigDecimal("75.50")))
        assertTrue(repo.delete(id))
        assertNull(repo.getById(id))
    }
}
