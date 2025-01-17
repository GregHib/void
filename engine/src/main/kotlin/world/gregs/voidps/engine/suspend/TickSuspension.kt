package world.gregs.voidps.engine.suspend

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.event.Context
import kotlin.coroutines.resume

data class TickSuspension(
    val tick: Int,
    override val onCancel: (() -> Unit)?,
    private val continuation: CancellableContinuation<Unit>
) : Suspension() {

    override fun ready(): Boolean {
        return GameLoop.tick >= tick
    }

    override fun resume() {
        super.resume()
        continuation.resume(Unit)
    }

    companion object {
        context(Context<*>) suspend operator fun invoke(ticks: Int) {
            if (ticks <= 0) {
                return
            }
            suspendCancellableCoroutine {
                character.suspension = TickSuspension(GameLoop.tick + ticks, onCancel, it)
            }
            character.suspension = null
        }
    }
}