package unitTests

import com.brickstemple.dto.users.UserDto
import com.brickstemple.fakeRepositories.FakeUserRepository
import com.brickstemple.util.HashUtil
import kotlin.test.*

class UserRepositoryTest {

    @Test
    fun `create - user is saved and password is hashed`() {
        val repo = FakeUserRepository()

        val id = repo.create(
            UserDto(username = "alice", email = "alice@mail.com", password = "123456")
        )

        val user = repo.getById(id)
        assertNotNull(user)
        assertEquals("alice", user.username)
        assertEquals("alice@mail.com", user.email)

        val rawUser = repo.getByEmail("alice@mail.com")!!
        assertNotEquals("123456", rawUser.password)
        assertTrue(HashUtil.checkPassword("123456", rawUser.password))
    }

    @Test
    fun `getByEmail - returns correct user`() {
        val repo = FakeUserRepository()
        repo.create(UserDto(username = "bob", email = "bob@mail.com", password = "qwerty"))

        val user = repo.getByEmail("bob@mail.com")
        assertNotNull(user)
        assertEquals("bob", user.username)
    }

    @Test
    fun `update - changes username and email, keeps createdAt`() {
        val repo = FakeUserRepository()
        val id = repo.create(UserDto(username = "oldname", email = "old@mail.com", password = "111111"))

        val beforeUpdate = repo.getById(id)!!.createdAt

        val result = repo.update(
            id,
            UserDto(username = "newname", email = "new@mail.com", password = "222222")
        )

        val updated = repo.getById(id)
        assertTrue(result)
        assertEquals("newname", updated!!.username)
        assertEquals("new@mail.com", updated.email)
        assertEquals(beforeUpdate, updated.createdAt)
        assertTrue(HashUtil.checkPassword("222222", repo.getByEmail("new@mail.com")!!.password))
    }

    @Test
    fun `delete - removes user and returns true`() {
        val repo = FakeUserRepository()
        val id = repo.create(UserDto(username = "charlie", email = "charlie@mail.com", password = "password"))

        assertTrue(repo.delete(id))
        assertNull(repo.getById(id))
    }

    @Test
    fun `getAll - returns all added users`() {
        val repo = FakeUserRepository()
        repo.create(UserDto(username = "u1", email = "u1@mail.com", password = "p112345678"))
        repo.create(UserDto(username = "u2", email = "u2@mail.com", password = "p212345678"))

        val result = repo.getAll()
        assertEquals(2, result.size)
        assertEquals("u1", result[0].username)
        assertEquals("u2", result[1].username)
    }
}
