package world.gregs.voidps.engine.suspend

import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.Context
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

interface SuspendableContext<C : Character> : Context<C> {
    /**
     * Interrupt-able pausing of scripts
     * Note: can't be used after a dialogue suspension in an interaction as the
     * interaction will have finished and there will be nothing to resume the suspension
     */
    suspend fun pause(ticks: Int = 1)

    /**
     * Prevents non-interface player input and most processing
     * Cannot be cancelled.
     */
    suspend fun delay(ticks: Int = 1) {
        if (ticks <= 0) {
            return
        }
        character["delay"] = ticks
        suspendCancellableCoroutine {
            character.delay = it
        }
    }

    /**
     * Delay until the appeared location of the character has moved [delta] in [delay] time
     */
    suspend fun Character.exactMoveDelay(delta: Delta, delay: Int = tile.distanceTo(tile.add(delta)) * 30, direction: Direction = Direction.NONE) {
        character.exactMove(delta, delay, direction)
        delay(delay / 30)
    }

    /**
     * Delay until the appeared location of the character has moved to [target] in [delay] time
     */
    suspend fun Character.exactMoveDelay(target: Tile, delay: Int = tile.distanceTo(target) * 30, direction: Direction = Direction.NONE, startDelay: Int = 0) {
        character.exactMove(target, delay, direction, startDelay)
        delay((startDelay + delay) / 30)
    }

    /**
     * Delay until characters animation [id] is complete
     * @param override the current animation
     */
    suspend fun Character.animDelay(id: String, override: Boolean = false) {
        val ticks = setAnimation(id, override = override)
        delay(ticks)
    }
}