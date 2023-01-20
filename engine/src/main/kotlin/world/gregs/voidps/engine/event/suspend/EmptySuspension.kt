package world.gregs.voidps.engine.event.suspend

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import kotlin.coroutines.resume

class EmptySuspension(
    private val continuation: CancellableContinuation<Unit>
) : EventSuspension {

    private var finished = false

    override fun ready(): Boolean {
        return false
    }

    override fun finished(): Boolean {
        return finished
    }

    override fun resume() {
        finished = true
        continuation.resume(Unit)
    }

    companion object {
        context(Interaction) suspend operator fun invoke(): Unit = suspendCancellableCoroutine {
            suspend = EmptySuspension(it)
        }
    }
}