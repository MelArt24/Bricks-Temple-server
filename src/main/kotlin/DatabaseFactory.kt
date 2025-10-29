package com.brickstemple

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection
import io.github.cdimascio.dotenv.dotenv

fun Application.configureDatabase() {

//    val dotenv = dotenv()
//    val dbUrl = dotenv["DB_URL"]
//    val dbUser = dotenv["DB_USER"]
//    val dbPassword = dotenv["DB_PASSWORD"]
//
//    Database.connect(
//        url = dbUrl ?: error("DB_URL not found"),
//        driver = "org.postgresql.Driver",
//        user = dbUser ?: error("DB_USER not found"),
//        password = dbPassword ?: error("DB_PASSWORD not found")
//    )

    val config = environment.config.config("ktor.database")
    Database.connect(
        url = config.property("url").getString(),
        driver = config.property("driver").getString(),
        user = config.property("user").getString(),
        password = config.property("password").getString()
    )


    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

    transaction {
//        exec("SELECT 1;") { rs ->
//            if (rs.next()) {
//                println("✅✅✅ Database test query succeeded!")
//            }
//        }
    }

//    log.info("✅✅✅ Connected to PostgreSQL database!")
}