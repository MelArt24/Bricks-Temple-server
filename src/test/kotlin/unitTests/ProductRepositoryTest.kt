package unitTests

import com.brickstemple.dto.products.ProductDto
import com.brickstemple.dto.products.ProductUpdateDto
import com.brickstemple.fakeRepositories.FakeProductRepository
import kotlin.test.*
import java.math.BigDecimal

class ProductRepositoryTest {

    @Test
    fun `create - saves product and returns id`() {
        val repo = FakeProductRepository()

        val id = repo.create(
            ProductDto(
                id = null,
                name = "Lego Car",
                category = "Vehicles",
                number = "4211",
                details = 243,
                minifigures = 0,
                age = "7+",
                year = "2024",
                size = "medium",
                condition = "new",
                price = BigDecimal("99.99"),
                createdAt = null,
                image = "car.png",
                description = "Cool Lego car",
                type = "lego",
                keywords = "car, vehicle, lego",
                isAvailable = true
            )
        )

        val created = repo.getById(id)
        assertNotNull(created)
        assertEquals("Lego Car", created.name)
        assertEquals(BigDecimal("99.99"), created.price)
        assertNotNull(created.createdAt)
    }

    @Test
    fun `getAll - returns all created products`() {
        val repo = FakeProductRepository()
        repo.create(ProductDto(null, "P1", "C1", "1", null, 0, null, null, null, "new",
            BigDecimal("10.0"), null, null, "lego", "set", "set, lego"))
        repo.create(ProductDto(null, "P2", "C2", "2", null, 0, null, null, null, "used",
            BigDecimal("20.0"), null, null, "lego", "set", "set, lego"))

        val all = repo.getAll()
        assertEquals(2, all.size)
    }

    @Test
    fun `getById - returns null for non-existing product`() {
        val repo = FakeProductRepository()
        assertNull(repo.getById(999))
    }

    @Test
    fun `update - only changes provided fields`() {
        val repo = FakeProductRepository()

        val id = repo.create(
            ProductDto(
                id = null,
                name = "Original",
                category = "OldCat",
                number = "100",
                details = null,
                minifigures = 0,
                age = null,
                year = null,
                size = null,
                condition = "new",
                price = BigDecimal("50.00"),
                createdAt = null,
                image = null,
                description = null,
                type = "lego",
                keywords = "test",
                isAvailable = true
            )
        )

        val success = repo.update(
            id,
            ProductUpdateDto(
                name = "Updated Name",
                price = BigDecimal("75.0"),
                category = null,
                condition = "used"
            )
        )

        assertTrue(success)

        val updated = repo.getById(id)
        assertEquals("Updated Name", updated!!.name)
        assertEquals(BigDecimal("75.0"), updated.price)
        assertEquals("OldCat", updated.category)
        assertEquals("used", updated.condition)
    }

    @Test
    fun `delete - removes product by id`() {
        val repo = FakeProductRepository()

        val id = repo.create(
            ProductDto(null, "Removable", "Cat", "200", null, 0, null, null, null, "new",
                BigDecimal("30.0"), null, null, "lego", "set", "set, lego")
        )

        val removed = repo.delete(id)
        assertTrue(removed)
        assertNull(repo.getById(id))
    }
}
