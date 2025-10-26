package world.gregs.voidps.engine.entity.character.mode.interact

import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.SuspendableEvent
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.engine.suspend.Suspension

abstract class Interaction<C : Character> :
    CancellableEvent(),
    SuspendableEvent,
    SuspendableContext<C> {
    var approach = false
    val operate: Boolean
        get() = !approach
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
        val delay = character.steps.last - GameLoop.tick
        if (delay <= 0) {
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
            while (!interact.arrived(range) && character.steps.isNotEmpty()) {
                delay(1)
            }
        }
    }
}


/**
 * Interrupt-able pausing
 * Note: can't be used after a dialogue suspension in an interaction as the
 * interaction will have finished and there will be nothing to resume the suspension
 */
suspend fun Character.pause(ticks: Int) {
    Suspension.start(this, ticks)
}


/**
 * Movement delay, typically operating/interacting with an object or floor item that performs an animation or exact movement
 */
suspend fun Character.arriveDelay() {
    val delay = steps.last - GameLoop.tick
    if (delay <= 0) {
        return
    }
    delay(delay)
}

/**
 * Prevents non-interface player input and most processing
 * Cannot be cancelled.
 */
suspend fun Character.delay(ticks: Int = 1) {
    if (ticks <= 0) {
        return
    }
    this["delay"] = ticks
    suspendCancellableCoroutine {
        delay = it
    }
}

/**
 * Set the range a player can interact with their target from
 */
suspend fun Character.approachRange(range: Int?, update: Boolean = true) {
    val interact = mode as? Interact ?: return
    interact.updateRange(range, update)
    if (range != null) {
        while (!interact.arrived(range) && steps.isNotEmpty()) {
            delay(1)
        }
    }
}
