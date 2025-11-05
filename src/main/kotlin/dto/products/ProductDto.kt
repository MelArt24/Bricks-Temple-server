package com.brickstemple.dto.products

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.time.LocalDateTime
import java.math.BigDecimal

@Serializable
data class ProductDto(
    val id: Int? = null,
    val name: String,
    val category: String,
    val number: Int,
    val details: Int? = null,
    val minifigures: Int? = null,
    val age: String? = null,
    val year: Int? = null,
    val size: String? = null,
    val condition: String,
    @Contextual val price: BigDecimal,
    @Contextual val createdAt: LocalDateTime? = null,
    val image: String? = null,
    val description: String? = null,
    val type: String,
    val keywords: String? = null,
    val isAvailable: Boolean = true
)
