package world.gregs.voidps.network.login.registration

import java.util.concurrent.ConcurrentHashMap

/**
 * Limits the number of account creation attempts per ip address within a fixed time window
 * @param limit maximum attempts per window, zero or less disables the limit
 */
class RegistrationLimiter(
    private val limit: Int,
    private val windowMillis: Long,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private class Window(val start: Long, val count: Int)

    private val attempts = ConcurrentHashMap<String, Window>()

    fun allow(address: String): Boolean {
        if (limit <= 0) {
            return true
        }
        val now = clock()
        val window = attempts.compute(address) { _, current ->
            if (current == null || now - current.start >= windowMillis) {
                Window(now, 1)
            } else {
                Window(current.start, current.count + 1)
            }
        }
        return window!!.count <= limit
    }

    /**
     * Milliseconds until [address]'s current window ends and it may try again, zero when it has no window
     */
    fun remaining(address: String): Long {
        val window = attempts[address] ?: return 0
        return (windowMillis - (clock() - window.start)).coerceAtLeast(0)
    }

    fun clear() {
        attempts.clear()
    }
}
