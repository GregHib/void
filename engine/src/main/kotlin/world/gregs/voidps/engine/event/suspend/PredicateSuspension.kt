package world.gregs.voidps.engine.event.suspend

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.entity.character.CharacterContext
import kotlin.coroutines.resume

class PredicateSuspension(
    private val predicate: () -> Boolean,
    private val continuation: CancellableContinuation<Unit>
) : EventSuspension {

    var boolean: Boolean? = null
    private var finished = false

    override fun ready(): Boolean {
        return predicate.invoke()
    }

    override fun finished(): Boolean {
        return finished
    }

    override fun resume() {
        finished = true
        continuation.resume(Unit)
    }

    companion object {
        context(CharacterContext) suspend operator fun invoke(predicate: () -> Boolean): Unit = suspendCancellableCoroutine {
            character.queue.suspend = PredicateSuspension(predicate, it)
        }
    }
}