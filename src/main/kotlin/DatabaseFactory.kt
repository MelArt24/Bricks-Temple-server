package com.brickstemple

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection

fun Application.configureDatabase() {
    val config = environment.config.config("database")

    Database.connect(
        url = config.property("url").getString(),
        driver = config.property("driver").getString(),
        user = config.property("user").getString(),
        password = config.property("password").getString()
    )

    // Це для уникнення блокувань транзакцій у Render
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

    transaction {
        // we  will create tables here
    }

    log.info("Connected to PostgreSQL database!")
}