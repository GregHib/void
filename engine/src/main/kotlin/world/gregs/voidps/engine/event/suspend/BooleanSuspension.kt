package world.gregs.voidps.engine.event.suspend

import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume

class BooleanSuspension(
    private val continuation: CancellableContinuation<Boolean>
) : EventSuspension {

    var boolean: Boolean? = null
    private var finished = false

    override fun ready(): Boolean {
        return boolean != null
    }

    override fun finished(): Boolean {
        return finished
    }

    override fun resume() {
        finished = true
        continuation.resume(boolean!!)
    }
}