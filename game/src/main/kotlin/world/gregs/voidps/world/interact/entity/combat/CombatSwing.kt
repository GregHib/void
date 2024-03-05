package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

/**
 * A turn in a combat scenario resulting one or many hits and a [delay] until the next turn
 */
class CombatSwing(
    val target: Character
) : CancellableEvent() {
    var delay: Int? = null

    override fun size() = 7

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_combat_swing"
        1 -> dispatcher.identifier
        2 -> if (dispatcher is Character) dispatcher.weapon.id else ""
        3 -> if (dispatcher is Character) dispatcher.fightStyle else "melee"
        4 -> if (dispatcher is Character) dispatcher.spell else ""
        5 -> if (dispatcher is Player) dispatcher.specialAttack else false
        6 -> delay != null // swung
        else -> null
    }
}

fun combatSwing(
    weapon: String = "*",
    style: String = "*",
    spell: String = "*",
    swung: Boolean? = false,
    special: Boolean? = false,
    override: Boolean = true,
    block: suspend CombatSwing.(Player) -> Unit
) {
    Events.handle("player_combat_swing", "player", weapon, style, spell, swung, special, override = override, handler = block)
}

fun npcCombatSwing(
    npc: String = "*",
    weapon: String = "*",
    style: String = "*",
    spell: String = "*",
    swung: Boolean? = false,
    override: Boolean = true,
    block: suspend CombatSwing.(NPC) -> Unit
) {
    Events.handle("npc_combat_swing", npc, weapon, style, spell, swung, "*", override = override, handler = block)
}

fun characterCombatSwing(
    weapon: String = "*",
    style: String = "*",
    spell: String = "*",
    swung: Boolean? = false,
    override: Boolean = true,
    block: suspend CombatSwing.(Character) -> Unit
) {
    combatSwing(weapon, style, spell, swung, null, override, block)
    npcCombatSwing("*", weapon, style, spell, swung, override, block)
}