package content.entity.combat

import content.skill.melee.weapon.fightStyle
import content.skill.melee.weapon.weapon
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

/**
 * A turn in a combat scenario resulting one or many hits
 */
class CombatSwing(
    val target: Character
) : CancellableEvent() {

    override val size = 4

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_combat_swing"
        1 -> dispatcher.identifier
        2 -> if (dispatcher is Character) dispatcher.weapon.id else ""
        3 -> if (dispatcher is Character) dispatcher.fightStyle else "melee"
        else -> null
    }
}

fun combatSwing(weapon: String = "*", style: String = "*", handler: suspend CombatSwing.(Player) -> Unit) {
    Events.handle("player_combat_swing", "player", weapon, style, handler = handler)
}

fun npcCombatSwing(npc: String = "*", style: String = "*", handler: suspend CombatSwing.(NPC) -> Unit) {
    Events.handle("npc_combat_swing", npc, "*", style, handler = handler)
}

fun characterCombatSwing(weapon: String = "*", style: String = "*", handler: suspend CombatSwing.(Character) -> Unit) {
    combatSwing(weapon, style, handler)
    npcCombatSwing("*", style, handler)
}