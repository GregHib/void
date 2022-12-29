package world.gregs.voidps.engine.event.suspend

import kotlinx.coroutines.CancellableContinuation

interface EventSuspension {
    val continuation: CancellableContinuation<*>
    fun ready(): Boolean
    fun resume()
}