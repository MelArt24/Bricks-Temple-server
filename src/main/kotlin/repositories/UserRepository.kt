package com.brickstemple.repositories

import com.brickstemple.dto.UserDto
import com.brickstemple.dto.UserResponseDto
import com.brickstemple.models.Users
import com.brickstemple.util.HashUtil
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

open class UserRepository {

    open fun getAll(): List<UserDto> = transaction {
        Users.selectAll().orderBy(Users.id to SortOrder.ASC).map {
            UserDto(
                id = it[Users.id],
                username = it[Users.username],
                email = it[Users.email],
                password = it[Users.password],
                role = it[Users.role],
                createdAt = it[Users.createdAt]
            )
        }
    }

    open fun getById(id: Int): UserResponseDto? = transaction {
        Users.select { Users.id eq id }.singleOrNull()?.let {
            UserResponseDto(
                id = it[Users.id],
                username = it[Users.username],
                email = it[Users.email],
                role = it[Users.role],
                createdAt = it[Users.createdAt]
            )
        }
    }

    open fun create(u: UserDto): Int = transaction {
        Users.insert {
            it[username] = u.username
            it[email] = u.email
            it[password] = HashUtil.hashPassword(u.password)
            it[role] = u.role
            it[createdAt] = LocalDateTime.now()
        } get Users.id
    }

    open fun update(id: Int, u: UserDto): Boolean = transaction {
        Users.update({ Users.id eq id }) {
            it[username] = u.username
            it[email] = u.email
            it[password] = u.password
            u.role?.let { roleValue -> it[role] = roleValue }
        } > 0
    }

    open fun delete(id: Int): Boolean = transaction {
        Users.deleteWhere { Users.id eq id } > 0
    }
}
