package com.brickstemple.fakeRepositories

import com.brickstemple.dto.products.ProductDto
import com.brickstemple.dto.products.ProductUpdateDto
import com.brickstemple.repositories.ProductRepository
import java.time.LocalDateTime

/**
 * Fake in-memory ProductRepository for testing without PostgreSQL.
 */
class FakeProductRepository : ProductRepository() {
    private val products = mutableListOf<ProductDto>()

    override fun getAll(): List<ProductDto> = products

    override fun getById(id: Int): ProductDto? = products.find { it.id == id }

    override fun create(p: ProductDto): Int {
        val newId = (products.maxOfOrNull { it.id ?: 0 } ?: 0) + 1
        val newProduct = p.copy(
            id = newId,
            createdAt = LocalDateTime.now(),
            price = p.price,
        )
        products.add(newProduct)
        return newId
    }

    override fun update(id: Int, product: ProductUpdateDto): Boolean {
        val existingIndex = products.indexOfFirst { it.id == id }
        if (existingIndex == -1) return false

        val old = products[existingIndex]
        val updated = old.copy(
            name = product.name ?: old.name,
            category = product.category ?: old.category,
            number = product.number ?: old.number,
            details = product.details ?: old.details,
            minifigures = product.minifigures ?: old.minifigures,
            age = product.age ?: old.age,
            year = product.year ?: old.year,
            size = product.size ?: old.size,
            condition = product.condition ?: old.condition,
            price = product.price ?: old.price,
            image = product.image ?: old.image,
            description = product.description ?: old.description,
            type = product.type ?: old.type,
            keywords = product.keywords ?: old.keywords,
            isAvailable = product.isAvailable ?: old.isAvailable
        )

        products[existingIndex] = updated
        return true
    }

    override fun delete(id: Int): Boolean = products.removeIf { it.id == id }
}
