package world.gregs.voidps.engine.event.suspend

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import kotlin.coroutines.resume

class StringSuspension(
    private val continuation: CancellableContinuation<String>
) : EventSuspension {

    var string: String? = null
    private var finished = false

    override fun ready(): Boolean {
        return string != null
    }

    override fun finished(): Boolean {
        return finished
    }

    override fun resume() {
        finished = true
        continuation.resume(string!!)
    }

    companion object {
        context(Interaction) suspend operator fun invoke(): String = suspendCancellableCoroutine {
            suspend = StringSuspension(it)
        }
    }
}