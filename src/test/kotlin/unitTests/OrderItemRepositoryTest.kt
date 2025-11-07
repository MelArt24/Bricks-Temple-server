package unitTests

import com.brickstemple.dto.order_items.OrderItemDto
import com.brickstemple.fakeRepositories.FakeOrderItemRepository
import kotlin.test.*
import java.math.BigDecimal

class OrderItemRepositoryTest {

    @Test
    fun `create - should add item to repository`() {
        val repo = FakeOrderItemRepository()
        val id = repo.create(
            OrderItemDto(orderId = 1, productId = 10, quantity = 2, priceAtPurchase = BigDecimal("99.99"))
        )
        assertEquals(1, id)
        assertEquals(1, repo.getByOrder(1).size)
    }

    @Test
    fun `getByOrder - returns only items of specific order`() {
        val repo = FakeOrderItemRepository()
        repo.create(OrderItemDto(orderId = 1, productId = 10, quantity = 2, priceAtPurchase = BigDecimal("50.0")))
        repo.create(OrderItemDto(orderId = 2, productId = 20, quantity = 1, priceAtPurchase = BigDecimal("75.0")))
        assertEquals(1, repo.getByOrder(1).size)
    }

    @Test
    fun `deleteByOrder - removes all items of an order`() {
        val repo = FakeOrderItemRepository()
        repo.create(OrderItemDto(orderId = 3, productId = 100, quantity = 1, priceAtPurchase = BigDecimal("30.0")))
        repo.create(OrderItemDto(orderId = 3, productId = 101, quantity = 2, priceAtPurchase = BigDecimal("40.0")))

        val deletedCount = repo.deleteByOrder(3)
        assertEquals(2, deletedCount)
        assertTrue(repo.getByOrder(3).isEmpty())
    }
}
