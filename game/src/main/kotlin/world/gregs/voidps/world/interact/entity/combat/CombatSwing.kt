package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.Event

/**
 * A turn in a combat scenario resulting one or many hits and a [delay] until the next turn
 */
class CombatSwing(
    val target: Character
) : Event {
    var delay: Int? = null

    fun swung(): Boolean {
        return delay != null
    }

}