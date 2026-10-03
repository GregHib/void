package world.gregs.voidps.network.login

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LoginAttemptsTest {

    private var now = 0L
    private val attempts = LoginAttempts(accountLimit = 2, addressLimit = 3, window = 100, clock = { now })

    @Test
    fun `Account blocked after too many failures`() {
        attempts.failed("Bob", "1.1.1.1")
        assertFalse(attempts.blocked("bob", "2.2.2.2"))
        attempts.failed("bob", "1.1.1.1")
        assertTrue(attempts.blocked("BOB", "2.2.2.2"))
    }

    @Test
    fun `Address blocked after too many failures`() {
        attempts.failed("a", "1.1.1.1")
        attempts.failed("b", "1.1.1.1")
        attempts.failed("c", "1.1.1.1")
        assertTrue(attempts.blocked("d", "1.1.1.1"))
        assertFalse(attempts.blocked("d", "2.2.2.2"))
    }

    @Test
    fun `Block expires after window`() {
        attempts.failed("bob", "1.1.1.1")
        attempts.failed("bob", "1.1.1.1")
        now = 100
        assertFalse(attempts.blocked("bob", "1.1.1.1"))
    }

    @Test
    fun `Success resets account failures`() {
        attempts.failed("bob", "1.1.1.1")
        attempts.succeeded("bob")
        attempts.failed("bob", "1.1.1.1")
        assertFalse(attempts.blocked("bob", "2.2.2.2"))
    }
}
