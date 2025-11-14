package com.brickstemple.repositories

import com.brickstemple.dto.products.ProductDto
import com.brickstemple.dto.products.ProductUpdateDto
import com.brickstemple.models.Products
import com.brickstemple.util.toProductDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.time.LocalDateTime

open class ProductRepository {

    open fun getAll(): List<ProductDto> = transaction {
        Products.selectAll().orderBy(Products.id to SortOrder.DESC).map { it.toProductDto() }
    }

    open fun getPaged(page: Int, limit: Int): List<ProductDto> = transaction {
        val offset = (page - 1) * limit
        Products
            .selectAll()
            .orderBy(Products.id to SortOrder.DESC)
            .limit(limit, offset.toLong())
            .map { it.toProductDto() }
    }

    open fun count(): Long = transaction {
        Products.selectAll().count()
    }


    open fun getById(id: Int): ProductDto? = transaction {
        Products.select { Products.id eq id }.singleOrNull()?.toProductDto()
    }

    open fun create(p: ProductDto): Int = transaction {
        Products.insert {
            it[name]        = p.name
            it[category]    = p.category
            it[number]      = p.number
            it[details]     = p.details
            it[minifigures] = p.minifigures
            it[age]         = p.age
            it[year]        = p.year
            it[size]        = p.size
            it[condition]   = p.condition
            it[price]       = p.price
            it[createdAt]   = LocalDateTime.now()
            it[image]       = p.image
            it[description] = p.description
            it[type]        = p.type
            it[keywords]    = p.keywords
            it[isAvailable] = p.isAvailable
        } get Products.id
    }

    fun update(id: Int, p: ProductDto): Boolean = transaction {
        Products.update({ Products.id eq id }) {
            it[name]        = p.name
            it[category]    = p.category
            it[number]      = p.number
            it[details]     = p.details
            it[minifigures] = p.minifigures
            it[age]         = p.age
            it[year]        = p.year
            it[size]        = p.size
            it[condition]   = p.condition
            it[price]       = p.price
            it[image]       = p.image
            it[description] = p.description
            it[type]        = p.type
            it[keywords]    = p.keywords
            it[isAvailable] = p.isAvailable
        } > 0
    }

    @Override
    open fun update(id: Int, product: ProductUpdateDto): Boolean {
        return transaction {
            val existing = Products.select { Products.id eq id }.singleOrNull()
                ?: return@transaction false

            Products.update({ Products.id eq id }) {
                product.name?.let { name -> it[Products.name] = name }
                product.category?.let { category -> it[Products.category] = category }
                product.number?.let { number -> it[Products.number] = number }
                product.details?.let { details -> it[Products.details] = details }
                product.minifigures?.let { minifigures -> it[Products.minifigures] = minifigures }
                product.age?.let { age -> it[Products.age] = age }
                product.year?.let { year -> it[Products.year] = year }
                product.size?.let { size -> it[Products.size] = size }
                product.condition?.let { condition -> it[Products.condition] = condition }
                product.price?.let { price -> it[Products.price] = price }
                product.image?.let { image -> it[Products.image] = image }
                product.description?.let { desc -> it[Products.description] = desc }
                product.type?.let { type -> it[Products.type] = type }
                product.keywords?.let { keywords -> it[Products.keywords] = keywords }
                product.isAvailable?.let { available -> it[isAvailable] = available }
            } > 0
        }
    }


    open fun delete(id: Int): Boolean = transaction {
        Products.deleteWhere { Products.id eq id } > 0
    }

    open fun getByType(type: String): List<ProductDto> = transaction {
        Products
            .select { Products.type eq type }
            .orderBy(Products.id to SortOrder.DESC)
            .map { it.toProductDto() }
    }

    open fun filter(
        type: String? = null,
        category: String? = null,
        search: String? = null,
        minPrice: BigDecimal? = null,
        maxPrice: BigDecimal? = null,
        year: String? = null,
        page: Int? = null,
        limit: Int? = null
    ): List<ProductDto> = transaction {

        var query = Products.selectAll()

        type?.let { query = query.andWhere { Products.type eq it } }
        category?.let { query = query.andWhere { Products.category eq it } }

        search?.let { s ->
            val q = "%${s.lowercase()}%"
            query = query.andWhere {
                (Products.name.lowerCase() like q) or
                        (Products.description.lowerCase() like q) or
                        (Products.keywords.lowerCase() like q)
            }
        }

        minPrice?.let { query = query.andWhere { Products.price greaterEq it } }
        maxPrice?.let { query = query.andWhere { Products.price lessEq it } }

        year?.let { query = query.andWhere { Products.year eq it } }

        query = query.orderBy(Products.id to SortOrder.DESC)

        if (page != null && limit != null && page > 0 && limit > 0) {
            val offset = (page - 1) * limit
            query = query.limit(limit, offset.toLong())
        }

        query.map { it.toProductDto() }
    }


}
