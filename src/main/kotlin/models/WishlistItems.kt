package com.brickstemple.models

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object WishlistItems : Table() {
    val id = integer("id").autoIncrement()
    val wishlistId = integer("wishlist_id").references(Wishlists.id, onDelete = ReferenceOption.CASCADE)
    val productId = integer("product_id").references(Products.id)
    val quantity = integer("quantity").default(1)
    val addedAt = datetime("added_at").clientDefault { LocalDateTime.now() }

    override val primaryKey = PrimaryKey(id)
}