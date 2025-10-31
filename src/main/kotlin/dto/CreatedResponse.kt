package com.brickstemple.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreatedResponse(val message: String, val id: Int)