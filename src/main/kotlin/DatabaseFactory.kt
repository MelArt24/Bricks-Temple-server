package com.brickstemple

import com.brickstemple.models.*
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.config.*
import com.typesafe.config.ConfigFactory

fun Application.configureDatabase() {
    val dotenv = dotenv {
        directory = System.getProperty("user.dir")
        ignoreIfMissing = true
    }

    val config = HoconApplicationConfig(ConfigFactory.load()).config("ktor.database")

    val dbUrl = config.propertyOrNull("url")?.getString() ?: dotenv["DB_URL"]
    val dbUser = config.propertyOrNull("user")?.getString() ?: dotenv["DB_USER"]
    val dbPassword = config.propertyOrNull("password")?.getString() ?: dotenv["DB_PASSWORD"]

    Database.connect(
        url = dbUrl ?: error("DB_URL not found"),
        driver = config.propertyOrNull("driver")?.getString() ?: "org.postgresql.Driver",
        user = dbUser ?: error("DB_USER not found"),
        password = dbPassword ?: error("DB_PASSWORD not found")
    )

    transaction {
        SchemaUtils.create(Products)
        SchemaUtils.create(Users)
        SchemaUtils.create(Orders)
        SchemaUtils.create(OrderItems)
        SchemaUtils.create(Wishlists)
        SchemaUtils.create(WishlistItems)
    }
}
