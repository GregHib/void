package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.SuspendableEvent
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.engine.suspend.Suspension

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
    fun approachRange(range: Int?, update: Boolean = true) {
        val interact = character.mode as? Interact ?: return
        interact.updateRange(range, update)
    }
}