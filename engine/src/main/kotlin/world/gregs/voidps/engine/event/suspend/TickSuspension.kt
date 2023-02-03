package world.gregs.voidps.engine.event.suspend

import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume

data class TickSuspension(
    var ticks: Int,
    private val continuation: CancellableContinuation<Unit>
) : EventSuspension {
    override fun ready(): Boolean {
        return --ticks == 0
    }

    override fun finished(): Boolean {
        return ticks <= 0
    }

    override fun resume() {
        continuation.resume(Unit)
    }
}