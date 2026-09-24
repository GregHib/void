package world.gregs.voidps.web

import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * A value that's too expensive to build per request, rebuilt at most once per [refreshMillis].
 *
 * Only the very first [get] waits for [build]; after that requests are always answered from the
 * last value built. Once it's older than [refreshMillis] the next request hands a rebuild to
 * [refresh] (a background thread by default) and keeps serving the stale value until it lands,
 * so no request ever pays for a rebuild and concurrent requests never trigger more than one. A
 * failed rebuild is logged and the old value kept; the next request tries again.
 */
class PeriodicSnapshot<T : Any>(
    private val name: String,
    private val refreshMillis: Long,
    private val clock: () -> Long = System::currentTimeMillis,
    private val refresh: (Runnable) -> Unit = { thread(isDaemon = true, name = "$name-snapshot") { it.run() } },
    private val build: () -> T,
) {
    class Entry<T>(val value: T, val builtAt: Long)

    @Volatile
    private var entry: Entry<T>? = null
    private val rebuilding = AtomicBoolean(false)

    fun get(): T = entry().value

    fun entry(): Entry<T> {
        val current = entry ?: return initial()
        if (clock() - current.builtAt >= refreshMillis && rebuilding.compareAndSet(false, true)) {
            refresh(Runnable { rebuild() })
        }
        return current
    }

    private fun initial(): Entry<T> = synchronized(this) {
        // Another request may have built it while this one waited for the lock.
        entry ?: Entry(build(), clock()).also { entry = it }
    }

    private fun rebuild() {
        try {
            entry = Entry(build(), clock())
        } catch (e: Exception) {
            logger.error("Failed to rebuild $name snapshot, serving the previous one.", e)
        } finally {
            rebuilding.set(false)
        }
    }

    fun clear() {
        entry = null
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PeriodicSnapshot::class.java)
    }
}
