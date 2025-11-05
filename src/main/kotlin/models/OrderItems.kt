package com.brickstemple.models

import org.jetbrains.exposed.sql.Table

object OrderItems : Table("order_items") {
    val id = integer("id").autoIncrement()
    val orderId = integer("order_id").references(Orders.id)
    val productId = integer("product_id").references(Products.id)
    val quantity = integer("quantity")
    val priceAtPurchase = decimal("price_at_purchase", 10, 2)

    override val primaryKey = PrimaryKey(id)
}
