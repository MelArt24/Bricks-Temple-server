package com.brickstemple.dto.users

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class UserDto(
    val id: Int? = null,
    val username: String,
    val email: String,
    val password: String,
    val role: String = "user",
    @Contextual val createdAt: LocalDateTime? = null
)
