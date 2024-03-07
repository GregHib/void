package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

/**
 * Prepare for combat by checking resources and calculating attack style against [target]
 */
class CombatPrepare(val target: Character) : CancellableEvent() {

    override val size = 3

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_combat_prepare"
        1 -> dispatcher.identifier
        2 -> if (dispatcher is Character) dispatcher.fightStyle else ""
        else -> null
    }
}

fun combatPrepare(style: String = "*", override: Boolean = true, block: suspend CombatPrepare.(Player) -> Unit) {
    Events.handle("player_combat_prepare", "player", style, override = override, handler = block)
}

fun npcCombatPrepare(npc: String = "*", style: String = "*", override: Boolean = true, block: suspend CombatPrepare.(NPC) -> Unit) {
    Events.handle("npc_combat_prepare", npc, style, override = override, handler = block)
}

fun characterCombatPrepare(style: String = "*", override: Boolean = true, block: suspend CombatPrepare.(Character) -> Unit) {
    combatPrepare(style, override, block)
    npcCombatPrepare("*", style, override, block)
}