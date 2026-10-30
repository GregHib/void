package world.gregs.voidps.network.login.registration

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RegistrationLimiterTest {

    private var now = 0L

    private fun limiter(limit: Int = 3, window: Long = 1000L) = RegistrationLimiter(limit, window) { now }

    @Test
    fun `Attempts within limit are allowed`() {
        val limiter = limiter()
        val address = "123.456.789"
        assertTrue(limiter.allow(address))
        assertTrue(limiter.allow(address))
        assertTrue(limiter.allow(address))

        assertFalse(limiter.allow(address))
    }

    @Test
    fun `Different addresses don't count against one another`() {
        val limiter = limiter()
        repeat(3) {
            assertTrue(limiter.allow("123.456.789"))
        }
        assertTrue(limiter.allow("192.168.1.1"))
    }

    @Test
    fun `Window resets after it expires`() {
        val limiter = limiter()
        val address = "123.456.789"
        repeat(3) {
            assertTrue(limiter.allow(address))
        }
        assertFalse(limiter.allow(address))
        now = 999L
        assertFalse(limiter.allow(address))
        now = 1000L
        assertTrue(limiter.allow(address))
    }

    @Test
    fun `Zero limit disables limiting`() {
        val limiter = limiter(limit = 0)
        repeat(10) {
            assertTrue(limiter.allow("123.456.789"))
        }
    }

    @Test
    fun `Clear removes all attempts`() {
        val limiter = limiter()
        repeat(3) {
            assertTrue(limiter.allow("123.456.789"))
        }
        limiter.clear()
        assertTrue(limiter.allow("123.456.789"))
    }
}
