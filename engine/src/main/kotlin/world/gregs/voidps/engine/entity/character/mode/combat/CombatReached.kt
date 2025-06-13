package world.gregs.voidps.engine.entity.character.mode.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.EventDispatcher

/**
 * Emitted when within attack range of combat target.
 */
class CombatReached(val target: Character) : CancellableEvent() {
    override val size = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "combat_reached"
        else -> null
    }
}
