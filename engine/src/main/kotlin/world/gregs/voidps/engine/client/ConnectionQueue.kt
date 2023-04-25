package world.gregs.voidps.engine.client

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.network.NetworkQueue
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

/**
 * Accepts up to [connectionsPerTickCap] accounts to connect or disconnect from the world.
 */
class ConnectionQueue(
    private val connectionsPerTickCap: Int,
) : NetworkQueue {
    private val waiting = ConcurrentHashMap.newKeySet<CancellableContinuation<Unit>>()
    private val disconnect = ConcurrentHashMap.newKeySet<() -> Unit>()

    fun disconnect(block: () -> Unit) {
        disconnect.add(block)
    }

    override suspend fun await() = suspendCancellableCoroutine<Unit> {
        waiting.add(it)
    }

    override fun run() {
        val disconnect = disconnect.iterator()
        while (disconnect.hasNext()) {
            disconnect.next().invoke()
            disconnect.remove()
        }
        val iterator = waiting.iterator()
        var count = 0
        while (iterator.hasNext() && count++ < connectionsPerTickCap) {
            val next = iterator.next()
            next.resume(Unit)
            iterator.remove()
        }
    }
}