package world.gregs.voidps.world.interact.entity.combat.hit

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

/**
 * Damage done to a [target]
 * Emitted on swing, where [CombatHit] is after the attack delay
 * @param type the combat type, typically: melee, range or magic
 * @param damage the damage inflicted upon the [target]
 * @param delay until hit in client ticks
 */
data class CombatAttack(
    val target: Character,
    val type: String,
    val damage: Int,
    val weapon: Item,
    val spell: String,
    val special: Boolean,
    val delay: Int
) : Event {
    var blocked = false

    override val all = true

    override val size = 5

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_combat_attack${if (special) "_special" else ""}"
        1 -> dispatcher.identifier
        2 -> weapon.id
        3 -> type
        4 -> spell
        else -> null
    }
}

fun combatAttack(
    weapon: String = "*",
    type: String = "*",
    spell: String = "*",
    special: Boolean = false,
    override: Boolean = true,
    block: suspend CombatAttack.(Player) -> Unit
) {
    Events.handle("player_combat_attack${if (special) "_special" else ""}", "player", weapon, type, spell, override = override, handler = block)
}

fun npcCombatAttack(
    npc: String = "*",
    weapon: String = "*",
    type: String = "*",
    spell: String = "*",
    special: Boolean = false,
    override: Boolean = true,
    block: suspend CombatAttack.(Player) -> Unit
) {
    Events.handle("npc_combat_attack${if (special) "_special" else ""}", npc, weapon, type, spell, override = override, handler = block)
}

fun characterCombatAttack(
    weapon: String = "*",
    type: String = "*",
    spell: String = "*",
    special: Boolean = false,
    override: Boolean = true,
    block: suspend CombatAttack.(Character) -> Unit
) {
    combatAttack(weapon, type, spell, special, override, block)
    npcCombatAttack("*", weapon, type, spell, special, override, block)
}