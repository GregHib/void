package world.gregs.voidps.engine.suspend

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.Context

interface SuspendableContext<C : Character> : Context<C> {
    suspend fun pause(ticks: Int = 1)
}