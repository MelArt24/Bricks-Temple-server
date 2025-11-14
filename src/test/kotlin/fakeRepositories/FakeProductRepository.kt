package com.brickstemple.fakeRepositories

import com.brickstemple.dto.products.ProductDto
import com.brickstemple.dto.products.ProductUpdateDto
import com.brickstemple.repositories.ProductRepository
import java.math.BigDecimal
import java.time.LocalDateTime

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

    override fun count(): Long = products.size.toLong()

    override fun getPaged(page: Int, limit: Int): List<ProductDto> {
        val from = (page - 1) * limit
        if (from >= products.size) return emptyList()
        return products.drop(from).take(limit)
    }

    override fun filter(
        type: String?,
        category: String?,
        search: String?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        year: String?,
        page: Int?,
        limit: Int?
    ): List<ProductDto> {

        var result = products.toList()

        type?.let { t ->
            result = result.filter { product -> product.type.equals(t, ignoreCase = true) }
        }

        category?.let { c ->
            result = result.filter { product -> product.category.equals(c, ignoreCase = true) }
        }

        search?.let { s ->
            val q = s.lowercase()
            result = result.filter {
                it.name.lowercase().contains(q) ||
                        (it.description?.lowercase()?.contains(q) ?: false) ||
                        (it.keywords?.lowercase()?.contains(q) ?: false)
            }
        }

        minPrice?.let { min ->
            result = result.filter { product -> product.price >= min }
        }

        maxPrice?.let { max ->
            result = result.filter { product -> product.price <= max }
        }

        year?.let { y ->
            result = result.filter { product -> product.year == y }
        }

        result = result.sortedByDescending { it.id ?: 0 }

        if (page != null && limit != null && page > 0 && limit > 0) {
            val from = (page - 1) * limit
            result = if (from < result.size)
                result.drop(from).take(limit)
            else
                emptyList()
        }

        return result
    }


}
