package com.brickstemple

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection
import io.github.cdimascio.dotenv.dotenv

fun Application.configureDatabase() {

    val dotenv = dotenv()
    val dbUrl = dotenv["DB_URL"]
    val dbUser = dotenv["DB_USER"]
    val dbPassword = dotenv["DB_PASSWORD"]

    Database.connect(
        url = dbUrl ?: error("DB_URL not found"),
        driver = "org.postgresql.Driver",
        user = dbUser ?: error("DB_USER not found"),
        password = dbPassword ?: error("DB_PASSWORD not found")
    )


    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

    transaction {
        // we will create tables here
    }

    log.info("Connected to PostgreSQL database!")
}