package world.gregs.voidps.engine.entity.character.mode.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

/**
 * Combat movement has stopped
 */
data class CombatStop(val target: Character) : Event {
    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_combat_stop"
        1 -> dispatcher.identifier
        else -> null
    }
}

fun combatStop(handler: suspend CombatStop.(Player) -> Unit) {
    Events.handle("player_combat_stop", "player", handler = handler)
}

fun npcCombatStop(npc: String = "*", handler: suspend CombatStop.(NPC) -> Unit) {
    Events.handle("npc_combat_stop", npc, handler = handler)
}

fun characterCombatStop(handler: suspend CombatStop.(Character) -> Unit) {
    combatStop(handler)
    npcCombatStop("*", handler)
}
