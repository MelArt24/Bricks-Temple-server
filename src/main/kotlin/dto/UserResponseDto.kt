package com.brickstemple.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class UserResponseDto(
    val id: Int,
    val username: String,
    val email: String,
    val role: String,
    @Contextual val createdAt: LocalDateTime? = null
)
