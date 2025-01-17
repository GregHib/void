package world.gregs.voidps.engine.suspend

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.event.Context
import kotlin.coroutines.resume

class PredicateSuspension(
    private val predicate: () -> Boolean,
    override val onCancel: (() -> Unit)?,
    private val continuation: CancellableContinuation<Unit>
) : Suspension() {

    var boolean: Boolean? = null

    override fun ready(): Boolean {
        return predicate.invoke()
    }

    override fun resume() {
        super.resume()
        continuation.resume(Unit)
    }

    companion object {
        context(Context<*>) suspend operator fun invoke(predicate: () -> Boolean) {
            if (predicate.invoke()) {
                return
            }
            suspendCancellableCoroutine {
                character.suspension = PredicateSuspension(predicate, onCancel, it)
            }
            character.suspension = null
        }
    }
}