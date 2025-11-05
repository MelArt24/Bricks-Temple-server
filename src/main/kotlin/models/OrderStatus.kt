package com.brickstemple.models

enum class OrderStatus(val value: String) {
    PENDING("pending"),
    CONFIRMED("confirmed"),
    DELIVERED("delivered"),
    CANCELLED("cancelled");
}
