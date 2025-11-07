package com.brickstemple.dto.products

@kotlinx.serialization.Serializable
data class PagedResponse<T>(
    val page: Int,
    val limit: Int,
    val total: Long,
    val data: List<T>
)
