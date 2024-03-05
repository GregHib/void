package world.gregs.voidps.engine.entity.character.mode.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher

/**
 * Combat movement has stopped
 */
data class CombatStop(val target: Character) : Event {
    override fun size() = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "combat_stop"
        else -> null
    }
}