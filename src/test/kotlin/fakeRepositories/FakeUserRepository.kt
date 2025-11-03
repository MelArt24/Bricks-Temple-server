package com.brickstemple.fakeRepositories

import com.brickstemple.dto.UserDto
import com.brickstemple.dto.UserResponseDto
import com.brickstemple.repositories.UserRepository
import com.brickstemple.util.HashUtil
import java.time.LocalDateTime

class FakeUserRepository : UserRepository() {

    private val users = mutableListOf<UserDto>()
    private var idCounter = 1

    override fun getAll(): List<UserDto> = users

    override fun getById(id: Int): UserResponseDto? =
        users.find { it.id == id }?.toResponse()

    override fun getByEmail(email: String): UserDto? =
        users.find { it.email == email }

    override fun create(u: UserDto): Int {
        val newUser = u.copy(
            id = idCounter++,
            password = HashUtil.hashPassword(u.password),
            createdAt = LocalDateTime.now()
        )
        users.add(newUser)
        return newUser.id!!
    }

    override fun update(id: Int, u: UserDto): Boolean {
        val index = users.indexOfFirst { it.id == id }
        if (index == -1) return false

        val existing = users[index]

        users[index] = u.copy(
            id = id,
            // Якщо пароль змінили → хешуємо
            password = if (u.password == existing.password) existing.password else HashUtil.hashPassword(u.password),
            createdAt = existing.createdAt
        )
        return true
    }

    override fun delete(id: Int): Boolean =
        users.removeIf { it.id == id }
}

private fun UserDto.toResponse(): UserResponseDto =
    UserResponseDto(
        id = this.id!!,
        username = this.username,
        email = this.email,
        role = this.role,
        createdAt = this.createdAt
    )
