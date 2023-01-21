package world.gregs.voidps.engine.event.suspend

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.coroutines.resume

class ContinueSuspension(
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
        suspend operator fun invoke(player: Player): Unit = suspendCancellableCoroutine {
            player.dialogueSuspension = ContinueSuspension(it)
        }
    }
}