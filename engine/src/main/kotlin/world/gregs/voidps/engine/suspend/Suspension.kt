package world.gregs.voidps.engine.suspend

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.entity.character.Character
import kotlin.coroutines.resume

data class Suspension(
    private val predicate: () -> Boolean,
) {
    private lateinit var continuation: CancellableContinuation<Unit>

    fun ready(): Boolean = predicate.invoke()

    fun resume() {
        continuation.resume(Unit)
    }

    companion object {
        suspend fun start(character: Character, predicate: () -> Boolean) {
            val suspension = Suspension(predicate)
            suspendCancellableCoroutine {
                suspension.continuation = it
                character.suspension = suspension
            }
            character.suspension = null
        }

        suspend fun start(character: Character, ticks: Int) {
            val tick = GameLoop.tick + ticks
            start(character) { GameLoop.tick >= tick }
        }
    }
}
