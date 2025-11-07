package unitTests

import com.brickstemple.util.HashUtil
import kotlin.test.*

class HashUtilTest {

    @Test
    fun `hashPassword - should not be equal to original password`() {
        val password = "secret123"
        val hashed = HashUtil.hashPassword(password)

        assertNotEquals(password, hashed, "Hashed password must differ from raw")
    }

    @Test
    fun `hashPassword - same password generates different hash (due to salt)`() {
        val password = "samepass"
        val hash1 = HashUtil.hashPassword(password)
        val hash2 = HashUtil.hashPassword(password)

        assertNotEquals(hash1, hash2, "HashUtil must generate unique salt for each hash")
    }

    @Test
    fun `checkPassword - returns true if password matches hash`() {
        val password = "mypassword"
        val hashed = HashUtil.hashPassword(password)

        assertTrue(HashUtil.checkPassword(password, hashed), "Password should match stored hash")
    }

    @Test
    fun `checkPassword - returns false for wrong password`() {
        val password = "correct"
        val hashed = HashUtil.hashPassword(password)

        assertFalse(HashUtil.checkPassword("wrong", hashed), "Wrong password should not match stored hash")
    }
}
