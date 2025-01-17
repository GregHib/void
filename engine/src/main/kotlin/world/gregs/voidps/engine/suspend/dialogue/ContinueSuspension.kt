package world.gregs.voidps.engine.suspend.dialogue

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.event.CharacterContext
import world.gregs.voidps.engine.entity.character.player.Player
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
        context(CharacterContext<Player>) suspend operator fun invoke() {
            suspendCancellableCoroutine {
                player.dialogueSuspension = ContinueSuspension(onCancel, it)
            }
            player.dialogueSuspension = null
        }
    }
}