package world.gregs.voidps.world.interact.entity.combat

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

fun combatSwing(
    weapon: String = "*",
    style: String = "*",
    override: Boolean = true,
    block: suspend CombatSwing.(Player) -> Unit
) {
    Events.handle("player_combat_swing", "player", weapon, style, override = override, handler = block)
}

fun npcCombatSwing(
    npc: String = "*",
    style: String = "*",
    override: Boolean = true,
    block: suspend CombatSwing.(NPC) -> Unit
) {
    Events.handle("npc_combat_swing", npc, "*", style, override = override, handler = block)
}

fun characterCombatSwing(
    weapon: String = "*",
    style: String = "*",
    override: Boolean = true,
    block: suspend CombatSwing.(Character) -> Unit
) {
    combatSwing(weapon, style, override, block)
    npcCombatSwing("*", style, override, block)
}