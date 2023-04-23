package world.gregs.voidps.engine.suspend

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.entity.character.CharacterContext
import kotlin.coroutines.resume

data class TickSuspension(
    var ticks: Int,
    override val onCancel: (() -> Unit)?,
    private val continuation: CancellableContinuation<Unit>
) : Suspension() {

    override fun ready(): Boolean {
        return --ticks == 0
    }

    override fun resume() {
        super.resume()
        continuation.resume(Unit)
    }

    companion object {
        context(CharacterContext) suspend operator fun invoke(ticks: Int) {
            if (ticks <= 0) {
                return
            }
            suspendCancellableCoroutine {
                character.suspension = TickSuspension(ticks, onCancel, it)
            }
            character.suspension = null
        }
    }
}