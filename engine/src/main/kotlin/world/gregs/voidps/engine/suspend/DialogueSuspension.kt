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

class ContinueSuspension : DialogueSuspension<Unit>() {
    companion object {
        suspend fun get(player: Player) = ContinueSuspension().get(player)
    }
}

class NameSuspension : DialogueSuspension<String>() {
    companion object {
        suspend fun get(player: Player) = NameSuspension().get(player)
    }
}

class StringSuspension : DialogueSuspension<String>() {
    companion object {
        suspend fun get(player: Player) = StringSuspension().get(player)
    }
}

class IntSuspension : DialogueSuspension<Int>() {
    companion object {
        suspend fun get(player: Player) = IntSuspension().get(player)
    }
}
