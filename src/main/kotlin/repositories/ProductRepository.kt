package com.brickstemple.repositories

import com.brickstemple.dto.ProductDto
import com.brickstemple.dto.ProductUpdateDto
import com.brickstemple.models.Products
import com.brickstemple.util.toProductDto
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

open class ProductRepository {

    open fun getAll(): List<ProductDto> = transaction {
        Products.selectAll().orderBy(Products.id to SortOrder.DESC).map { it.toProductDto() }
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
            } > 0
        }
    }


    open fun delete(id: Int): Boolean = transaction {
        Products.deleteWhere { Products.id eq id } > 0
    }
}
