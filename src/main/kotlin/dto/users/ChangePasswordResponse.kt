package com.brickstemple.dto.users

import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordResponse(
    val message: String
)
