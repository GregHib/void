package world.gregs.voidps.engine.suspend

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.coroutines.resume

class IntSuspension(
    private val continuation: CancellableContinuation<Int>
) : Suspension() {

    override var dialogue = true
    var int: Int? = null

    override fun ready(): Boolean {
        return !finished && int != null
    }

    override fun resume() {
        super.resume()
        continuation.resume(int!!)
    }

    companion object {
        suspend operator fun invoke(player: Player): Int = suspendCancellableCoroutine {
            player.suspension = IntSuspension(it)
        }
    }
}