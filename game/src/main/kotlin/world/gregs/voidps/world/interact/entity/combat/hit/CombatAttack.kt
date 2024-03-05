package world.gregs.voidps.world.interact.entity.combat.hit

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.*

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
    override fun size() = 6

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_combat_attack${if (special) "_special" else ""}"
        1 -> dispatcher.identifier
        2 -> weapon.id
        3 -> type
        4 -> spell
        5 -> true // prioritise non-overrides
        else -> null
    }
}

fun combatAttack(weapon: String = "*", type: String = "*", spell: String = "*", special: Boolean = false, override: Boolean = true, block: suspend CombatAttack.(Player) -> Unit) {
    Events.handle("player_combat_attack${if (special) "_special" else ""}", "player", weapon, type, spell, if (override) "*" else true, override = override, handler = block)
}

fun npcCombatAttack(npc: String = "*", weapon: String = "*", type: String = "*", spell: String = "*", special: Boolean = false, override: Boolean = true, block: suspend CombatAttack.(Player) -> Unit) {
    Events.handle("npc_combat_attack${if (special) "_special" else ""}", npc, weapon, type, spell, if (override) "*" else true, override = override, handler = block)
}

fun characterCombatAttack(weapon: String = "*", type: String = "*", spell: String = "*", special: Boolean = false, override: Boolean = true, block: suspend CombatAttack.(Player) -> Unit) {
    combatAttack(weapon, type, spell, special, override, block)
    npcCombatAttack("*", weapon, type, spell, special, override, block)
}

fun combatAttack(priority: Priority = Priority.MEDIUM, block: suspend CombatAttack.(Player) -> Unit) {
    on<CombatAttack>(priority = priority, block = block)
}

fun characterCombatAttack(block: suspend CombatAttack.(Character) -> Unit) {
    onCharacter<CombatAttack>(block = block)
}

fun characterSpellAttack(spell: String = "*", block: suspend CombatAttack.(Character) -> Unit) {
    onCharacter<CombatAttack>({ damage > 0 && type == "magic" && wildcardEquals(spell, this.spell) }, block = block)
}

fun characterSpellAttack(spells: Set<String>, block: suspend CombatAttack.(Character) -> Unit) {
    if (spells.any { it.contains("*") || it.contains("#") }) {
        throw IllegalArgumentException("Spell collections cannot contain wildcards.")
    }
    onCharacter<CombatAttack>({ damage > 0 && type == "magic" && spells.contains(spell) }, block = block)
}