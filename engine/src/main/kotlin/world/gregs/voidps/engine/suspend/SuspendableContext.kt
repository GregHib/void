package world.gregs.voidps.engine.suspend

import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.Context

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
}