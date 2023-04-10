package world.gregs.voidps.engine.suspend.dialogue

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.entity.character.player.PlayerContext
import world.gregs.voidps.engine.suspend.Suspension
import kotlin.coroutines.resume

class StringSuspension(
    override val onCancel: (() -> Unit)?,
    private val continuation: CancellableContinuation<String>
) : Suspension() {

    var string: String? = null

    override fun ready(): Boolean {
        return string != null
    }

    override fun resume() {
        super.resume()
        continuation.resume(string!!)
    }

    companion object {
        context(PlayerContext) suspend operator fun invoke(): String = suspendCancellableCoroutine {
            player.dialogueSuspension = StringSuspension(onCancel, it)
        }
    }
}