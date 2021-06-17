package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.event.Event

class CombatSwing(
    val target: Character
) : Event {
    var delay: Int = -1

    fun swung(): Boolean {
        return delay > 0
    }

}