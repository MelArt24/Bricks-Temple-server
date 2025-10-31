package com.brickstemple.fakeRepositories

import com.brickstemple.dto.UserDto
import com.brickstemple.dto.UserResponseDto
import com.brickstemple.repositories.UserRepository
import java.time.LocalDateTime

class FakeUserRepository : UserRepository() {

    private val users = mutableListOf<UserDto>()
    private var idCounter = 1

    override fun getAll(): List<UserDto> = users

    override fun getById(id: Int): UserResponseDto? =
        users.find { it.id == id }?.let {
            UserResponseDto(
                id = it.id!!,
                username = it.username,
                email = it.email,
                role = it.role,
                createdAt = it.createdAt!!
            )
        }


    override fun create(u: UserDto): Int {
        val user = u.copy(
            id = idCounter++,
            createdAt = LocalDateTime.now()
        )
        users.add(user)
        return user.id!!
    }

    override fun update(id: Int, u: UserDto): Boolean {
        val index = users.indexOfFirst { it.id == id }
        if (index == -1) return false
        users[index] = u.copy(id = id, createdAt = users[index].createdAt)
        return true
    }

    override fun delete(id: Int): Boolean = users.removeIf { it.id == id }
}
