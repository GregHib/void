package world.gregs.voidps.engine.suspend

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.entity.character.player.Player
import kotlin.coroutines.resume

sealed class DialogueSuspension<T : Any> {
    private lateinit var continuation: CancellableContinuation<T>

    fun resume(value: T) {
        continuation.resume(value)
    }

    suspend fun get(player: Player): T {
        val int = suspendCancellableCoroutine {
            continuation = it
            player.dialogueSuspension = this
        }
        player.dialogueSuspension = null
        return int
    }
}

data object ContinueSuspension : DialogueSuspension<Unit>()

data object StringSuspension : DialogueSuspension<String>()

data object IntSuspension : DialogueSuspension<Int>()
