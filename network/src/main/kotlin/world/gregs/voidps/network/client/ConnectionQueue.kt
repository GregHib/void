package world.gregs.voidps.network.client

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

/**
 * Accepts up to [connectionsPerTickCap] accounts to connect or disconnect from the world.
 */
class ConnectionQueue(
    private val connectionsPerTickCap: Int,
) : Runnable {
    private val waiting = ConcurrentHashMap.newKeySet<CancellableContinuation<Unit>>()
    private val disconnect = ConcurrentHashMap.newKeySet<() -> Unit>()

    fun disconnect(block: () -> Unit) {
        disconnect.add(block)
    }

    suspend fun await(): Unit = suspendCancellableCoroutine {
        waiting.add(it)
    }

    override fun run() {
        val disconnect = disconnect.iterator()
        while (disconnect.hasNext()) {
            val block = disconnect.next()
            disconnect.remove()
            try {
                block.invoke()
            } catch (e: Exception) {
                logger.error(e) { "Error disconnecting client." }
            }
        }
        val iterator = waiting.iterator()
        var count = 0
        while (iterator.hasNext() && count++ < connectionsPerTickCap) {
            val next = iterator.next()
            next.resume(Unit)
            iterator.remove()
        }
    }

    companion object {
        private val logger = InlineLogger()
    }
}
