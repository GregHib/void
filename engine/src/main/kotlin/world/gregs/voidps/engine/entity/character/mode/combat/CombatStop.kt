package world.gregs.voidps.engine.entity.character.mode.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

/**
 * Combat movement has stopped
 */
data class CombatStop(val target: Character) : Event {
    override fun size() = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_combat_stop"
        1 -> dispatcher.identifier
        else -> null
    }
}

fun combatStop(override: Boolean = true, handler: suspend CombatStart.(Player) -> Unit) {
    Events.handle("player_combat_stop", "player", override = override, handler = handler)
}

fun npcCombatStop(npc: String = "*", override: Boolean = true, handler: suspend CombatStart.(Player) -> Unit) {
    Events.handle("npc_combat_stop", npc, override = override, handler = handler)
}

fun characterCombatStop(override: Boolean = true, handler: suspend CombatStart.(Character) -> Unit) {
    combatStop(override, handler)
    npcCombatStop("*", override, handler)
}