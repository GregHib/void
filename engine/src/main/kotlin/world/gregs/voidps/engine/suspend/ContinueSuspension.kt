package world.gregs.voidps.engine.suspend

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.coroutines.resume

class ContinueSuspension(
    private val continuation: CancellableContinuation<Unit>
) : Suspension() {
    override var dialogue = true

    override fun ready(): Boolean {
        return false
    }

    override fun resume() {
        super.resume()
        continuation.resume(Unit)
    }

    companion object {
        suspend operator fun invoke(player: Player): Unit = suspendCancellableCoroutine {
            player.suspension = ContinueSuspension(it)
        }
    }
}