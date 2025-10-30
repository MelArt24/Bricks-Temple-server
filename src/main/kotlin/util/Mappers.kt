package com.brickstemple.util

import com.brickstemple.models.Products
import com.brickstemple.dto.ProductDto
import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toProductDto() = ProductDto(
    id = this[Products.id],
    name = this[Products.name],
    category = this[Products.category],
    number = this[Products.number],
    details = this[Products.details],
    minifigures = this[Products.minifigures],
    age = this[Products.age],
    year = this[Products.year],
    size = this[Products.size],
    condition = this[Products.condition],
    price = this[Products.price],
    createdAt = this[Products.createdAt],
    image = this[Products.image],
    description = this[Products.description],
    type = this[Products.type],
    keywords = this[Products.keywords]
)
