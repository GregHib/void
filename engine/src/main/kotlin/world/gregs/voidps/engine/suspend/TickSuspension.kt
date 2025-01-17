package world.gregs.voidps.engine.suspend

import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.entity.character.Character

data class TickSuspension(
    private val tick: Int,
) : Suspension() {

    override fun ready(): Boolean {
        return GameLoop.tick >= tick
    }

    companion object {
        suspend fun start(character: Character, ticks: Int) {
            if (ticks <= 0) {
                return
            }
            val suspension = TickSuspension(GameLoop.tick + ticks)
            suspendCancellableCoroutine {
                suspension.continuation = it
                character.suspension = suspension
            }
            character.suspension = null
        }
    }
}