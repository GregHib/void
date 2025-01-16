package world.gregs.voidps.engine.suspend.dialogue

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.Suspension
import kotlin.coroutines.resume

class IntSuspension(
    override val onCancel: (() -> Unit)?,
    private val continuation: CancellableContinuation<Int>
) : Suspension() {

    var int: Int? = null

    override fun ready(): Boolean {
        return int != null
    }

    override fun resume() {
        super.resume()
        continuation.resume(int!!)
    }

    companion object {
        context(CharacterContext<Player>) suspend operator fun invoke(): Int {
            val int = suspendCancellableCoroutine {
                player.dialogueSuspension = IntSuspension(onCancel, it)
            }
            player.dialogueSuspension = null
            return int
        }
    }
}