package world.gregs.voidps.network.login

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Tracks failed login attempts per account and per ip address, blocking further attempts
 * once a limit is reached until [window] has passed since the first failure
 */
class LoginAttempts(
    private val accountLimit: Int = 5,
    private val addressLimit: Int = 20,
    private val window: Long = TimeUnit.MINUTES.toMillis(5),
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private class Attempts(var count: Int, val start: Long)

    private val accounts = ConcurrentHashMap<String, Attempts>()
    private val addresses = ConcurrentHashMap<String, Attempts>()

    fun blocked(account: String, address: String): Boolean = exceeded(accounts, account.lowercase(), accountLimit) || exceeded(addresses, address, addressLimit)

    fun failed(account: String, address: String) {
        increment(accounts, account.lowercase())
        increment(addresses, address)
    }

    fun succeeded(account: String) {
        accounts.remove(account.lowercase())
    }

    private fun exceeded(map: ConcurrentHashMap<String, Attempts>, key: String, limit: Int): Boolean {
        val attempts = map[key] ?: return false
        if (expired(attempts, clock())) {
            map.remove(key, attempts)
            return false
        }
        return attempts.count >= limit
    }

    private fun increment(map: ConcurrentHashMap<String, Attempts>, key: String) {
        val now = clock()
        if (map.size >= PRUNE_THRESHOLD) {
            map.values.removeIf { expired(it, now) }
        }
        map.compute(key) { _, attempts ->
            if (attempts == null || expired(attempts, now)) {
                Attempts(1, now)
            } else {
                attempts.count++
                attempts
            }
        }
    }

    private fun expired(attempts: Attempts, now: Long) = now - attempts.start >= window

    companion object {
        private const val PRUNE_THRESHOLD = 10_000
    }
}
