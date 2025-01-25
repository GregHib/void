package content.entity.combat

import content.skill.melee.weapon.fightStyle
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

    override val notification: Boolean = true

    override val size = 3

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_combat_prepare"
        1 -> dispatcher.identifier
        2 -> if (dispatcher is Character) dispatcher.fightStyle else ""
        else -> null
    }
}

fun combatPrepare(style: String = "*", handler: suspend CombatPrepare.(Player) -> Unit) {
    Events.handle("player_combat_prepare", "player", style, handler = handler)
}

fun npcCombatPrepare(npc: String = "*", style: String = "*", handler: suspend CombatPrepare.(NPC) -> Unit) {
    Events.handle("npc_combat_prepare", npc, style, handler = handler)
}

fun characterCombatPrepare(style: String = "*", handler: suspend CombatPrepare.(Character) -> Unit) {
    combatPrepare(style, handler)
    npcCombatPrepare("*", style, handler)
}