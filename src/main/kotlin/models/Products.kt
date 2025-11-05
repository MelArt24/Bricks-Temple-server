package com.brickstemple.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Products : Table("products") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val category = varchar("category", 100)
    val number = integer("number")
    val details = integer("details").nullable()
    val minifigures = integer("minifigures").nullable()
    val age = varchar("age", 16).nullable()          // "3+", "18+"
    val year = integer("year").nullable()
    val size = varchar("size", 64).nullable()        // "70/79/27"
    val condition = varchar("condition", 32)         // "New", "Used", "Sealed"
    val price = decimal("price", 10, 2)
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val image = varchar("image", 512).nullable()    // URL
    val description = text("description").nullable()
    val type = varchar("type", 32)                  // "set", "minifigure" etc
    val keywords = text("keywords").nullable()
    val isAvailable = bool("is_available").default(true)

    override val primaryKey = PrimaryKey(id)
}
