package unitTests

import com.brickstemple.fakeRepositories.FakeWishlistRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FakeWishlistRepositoryTest {

    @Test
    fun `create - generates new wishlist with incrementing ID`() {
        val repo = FakeWishlistRepository()

        val id1 = repo.create(userId = 1)
        val id2 = repo.create(userId = 2)

        assertEquals(1, id1)
        assertEquals(2, id2)

        val w1 = repo.getByUser(1)
        val w2 = repo.getByUser(2)

        assertNotNull(w1)
        assertNotNull(w2)

        assertEquals(1, w1!!.id)
        assertEquals(2, w2!!.id)
    }

    @Test
    fun `create - if called twice for same user, returns same wishlist`() {
        val repo = FakeWishlistRepository()

        val id1 = repo.create(userId = 5)
        val id2 = repo.create(userId = 5)

        assertEquals(id1, id2)

        val w = repo.getByUser(5)
        assertNotNull(w)
        assertEquals(id1, w!!.id)
        assertEquals(5, w.userId)
    }

    @Test
    fun `getByUser - returns null if no wishlist created`() {
        val repo = FakeWishlistRepository()

        val result = repo.getByUser(999)
        assertNull(result)
    }

    @Test
    fun `clear - removes all wishlists`() {
        val repo = FakeWishlistRepository()
        repo.create(1)
        repo.create(2)

        assertNotNull(repo.getByUser(1))
        repo.clear()

        assertNull(repo.getByUser(1))
        assertNull(repo.getByUser(2))
    }
}
