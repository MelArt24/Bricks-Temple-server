package com.brickstemple.dto.products

import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import java.time.LocalDateTime
import java.math.BigDecimal

@Serializable
data class ProductUpdateDto(
    val name: String? = null,
    val category: String? = null,
    val number: Int? = null,
    val details: Int? = null,
    val minifigures: Int? = null,
    val age: String? = null,
    val year: Int? = null,
    val size: String? = null,
    val condition: String? = null,
    @Contextual val price: BigDecimal? = null,
    @Contextual val createdAt: LocalDateTime? = null,
    val image: String? = null,
    val description: String? = null,
    val type: String? = null,
    val keywords: String? = null,
    val isAvailable: Boolean = true
)
