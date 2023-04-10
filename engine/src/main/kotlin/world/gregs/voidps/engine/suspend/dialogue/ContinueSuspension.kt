package world.gregs.voidps.engine.suspend.dialogue

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.entity.character.player.PlayerContext
import world.gregs.voidps.engine.suspend.Suspension
import kotlin.coroutines.resume

class ContinueSuspension(
    override val onCancel: (() -> Unit)?,
    private val continuation: CancellableContinuation<Unit>
) : Suspension() {

    override fun ready(): Boolean {
        return false
    }

    override fun resume() {
        super.resume()
        continuation.resume(Unit)
    }

    companion object {
        context(PlayerContext) suspend operator fun invoke(): Unit = suspendCancellableCoroutine {
            player.dialogueSuspension = ContinueSuspension(onCancel, it)
        }
    }
}