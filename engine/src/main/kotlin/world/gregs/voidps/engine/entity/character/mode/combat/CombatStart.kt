package world.gregs.voidps.engine.entity.character.mode.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

/**
 * Combat has started
 */
data class CombatStart(val target: Character) : Event {
    override fun size() = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_combat_start"
        1 -> dispatcher.identifier
        else -> null
    }
}

fun combatStart(override: Boolean = true, handler: suspend CombatStart.(Player) -> Unit) {
    Events.handle("player_combat_start", "player", override = override, handler = handler)
}

fun npcCombatStart(npc: String = "*", override: Boolean = true, handler: suspend CombatStart.(Player) -> Unit) {
    Events.handle("npc_combat_start", npc, override = override, handler = handler)
}

fun characterCombatStart(override: Boolean = true, handler: suspend CombatStart.(Character) -> Unit) {
    combatStart(override, handler)
    npcCombatStart("*", override, handler)
}