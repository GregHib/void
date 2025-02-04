package world.gregs.voidps.engine.suspend

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.event.Context
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import kotlin.math.log

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
        exactMove(delta, delay, direction)
        delay(delay / 30)
    }

    /**
     * Delay until the appeared location of the character has moved to [target] in [delay] time
     */
    suspend fun Character.exactMoveDelay(target: Tile, delay: Int = tile.distanceTo(target) * 30, direction: Direction = Direction.NONE, startDelay: Int = 0) {
        exactMove(target, delay, direction, startDelay)
        delay((startDelay + delay) / 30)
    }

    /**
     * Delay until characters animation [id] is complete
     * @param override the current animation
     */
    suspend fun Character.animDelay(id: String, override: Boolean = false) {
        val ticks = anim(id, override = override)
        delay(ticks)
    }

    /**
     * Forces the character to walk to a tile
     */
    suspend fun Character.walkToDelay(tile: Tile, forceWalk: Boolean = false) {
        walkTo(tile, noCollision = false, forceWalk = forceWalk)
        delayTarget(tile)
    }

    /**
     * Force a character to walk to tile ignoring collisions
     */
    suspend fun Character.walkOverDelay(tile: Tile, forceWalk: Boolean = true) {
        walkTo(tile, noCollision = true, forceWalk = forceWalk)
        delayTarget(tile)
    }

    private suspend fun Character.delayTarget(target: Tile) {
        var count = 0
        if (tile.distanceTo(target) >= 50) {
            logger.warn { "Skipping target delay distance too great char=$this, target=$target, context=${this@SuspendableContext}" }
            return
        }
        while (tile != target && count++ < 50 && mode != EmptyMode) {
            delay()
        }
        if (count >= 50) {
            logger.warn { "Failed delaying target char=$this, target=$target, context=${this@SuspendableContext}" }
        }
    }

    companion object {
        private val logger = InlineLogger()
    }
}