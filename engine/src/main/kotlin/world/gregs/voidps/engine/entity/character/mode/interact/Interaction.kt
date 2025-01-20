package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setExactMovement
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.SuspendableEvent
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

abstract class Interaction<C : Character> : CancellableEvent(), SuspendableEvent, SuspendableContext<C> {
    var approach = false
    val operate: Boolean
        get() = !approach
    override var onCancel: (() -> Unit)? = null
    var launched = false

    abstract fun copy(approach: Boolean): Interaction<C>

    /**
     * Interrupt-able pausing
     * Note: can't be used after a dialogue suspension in an interaction as the
     * interaction will have finished and there will be nothing to resume the suspension
     */
    override suspend fun pause(ticks: Int) {
        Suspension.start(character, ticks)
    }

    /**
     * Movement delay, typically operating/interacting with an object or floor item that performs an animation or exact movement
     */
    suspend fun arriveDelay() {
        val delay = character.remaining("last_movement")
        if (delay == -1) {
            return
        }
        delay(delay)
    }

    /**
     * Set the range a player can interact with their target from
     */
    suspend fun approachRange(range: Int?, update: Boolean = true) {
        val interact = character.mode as? Interact ?: return
        interact.updateRange(range, update)
        if (range != null) {
            while (!interact.arrived(range)) {
                delay(1)
            }
        }
    }

    suspend fun Character.exactMove(target: Tile, delay: Int = tile.distanceTo(target) * 30, direction: Direction = Direction.NONE) {
        val start = tile
        tele(target)
        setExactMovement(Delta.EMPTY, delay, start.delta(tile), direction = direction)
        delay(delay / 30)
    }

    suspend fun Character.playAnimation(id: String, override: Boolean = false) {
        val ticks = setAnimation(id, override = override)
        delay(ticks)
    }
}