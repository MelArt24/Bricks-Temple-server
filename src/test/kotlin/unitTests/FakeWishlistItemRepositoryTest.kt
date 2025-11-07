package unitTests

import com.brickstemple.fakeRepositories.FakeWishlistItemRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FakeWishlistItemRepositoryTest {

    @Test
    fun `addOrIncrement - first add creates item with quantity 1`() {
        val items = FakeWishlistItemRepository()
        items.addOrIncrement(wishlistId = 1, productId = 100)

        val result = items.getByWishlist(1)
        assertEquals(1, result.size)
        assertEquals(1, result[0].quantity)
        assertEquals(100, result[0].productId)
    }

    @Test
    fun `addOrIncrement - same product increments quantity`() {
        val items = FakeWishlistItemRepository()
        items.addOrIncrement(1, 100)
        items.addOrIncrement(1, 100)

        val result = items.getByWishlist(1)
        assertEquals(1, result.size)
        assertEquals(2, result[0].quantity)
    }

    @Test
    fun `updateQuantity - sets exact quantity`() {
        val items = FakeWishlistItemRepository()
        items.addOrIncrement(1, 200)

        val id = items.getByWishlist(1)[0].id!!
        val updated = items.updateQuantity(id, 5)

        assertTrue(updated)
        assertEquals(5, items.getById(id)!!.quantity)
    }

    @Test
    fun `decrementOrDelete - decreases quantity if above 1`() {
        val items = FakeWishlistItemRepository()
        items.addOrIncrement(1, 300)
        items.addOrIncrement(1, 300)

        val id = items.getByWishlist(1)[0].id!!
        items.decrementOrDelete(id)

        assertEquals(1, items.getById(id)!!.quantity)
    }

    @Test
    fun `decrementOrDelete - deletes when quantity equals 1`() {
        val items = FakeWishlistItemRepository()
        items.addOrIncrement(1, 400)

        val id = items.getByWishlist(1)[0].id!!
        val deleted = items.decrementOrDelete(id)

        assertTrue(deleted)
        assertNull(items.getById(id))
    }

    @Test
    fun `delete - returns true if item existed`() {
        val items = FakeWishlistItemRepository()
        items.addOrIncrement(1, 500)

        val id = items.getByWishlist(1)[0].id!!
        val result = items.delete(id)

        assertTrue(result)
        assertTrue(items.getByWishlist(1).isEmpty())
    }

    @Test
    fun `clearWishlist - removes all items of given wishlist`() {
        val items = FakeWishlistItemRepository()
        items.addOrIncrement(1, 10)
        items.addOrIncrement(1, 11)
        items.addOrIncrement(2, 12)

        items.clearWishlist(1)

        assertTrue(items.getByWishlist(1).isEmpty())
        assertEquals(1, items.getByWishlist(2).size) // items of wishlist 2 remain
    }
}
