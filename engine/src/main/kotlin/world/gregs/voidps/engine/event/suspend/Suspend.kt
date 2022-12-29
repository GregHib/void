package world.gregs.voidps.engine.event.suspend

import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.event.SuspendableEvent

suspend fun SuspendableEvent.delay(ticks: Int = 1) {
    suspendCancellableCoroutine {
        events.suspend = TickSuspension(ticks, it)
    }
}