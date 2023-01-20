package world.gregs.voidps.engine.event.suspend

import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume

class IntSuspension(
    private val continuation: CancellableContinuation<Int>
) : EventSuspension {

    var int: Int? = null
    private var finished = false

    override fun ready(): Boolean {
        return int != null
    }

    override fun finished(): Boolean {
        return finished
    }

    override fun resume() {
        finished = true
        continuation.resume(int!!)
    }
}