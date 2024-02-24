package world.gregs.voidps.world.interact.entity.combat.hit

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.onCharacter
import world.gregs.voidps.engine.event.onNPC
import world.gregs.voidps.engine.event.wildcardEquals
import world.gregs.voidps.world.interact.entity.combat.weapon

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
}

fun combatAttack(priority: Priority = Priority.MEDIUM, block: suspend CombatAttack.(Player) -> Unit) {
    on<CombatAttack>(priority = priority, block = block)
}

fun npcCombatAttack(npc: String = "*", priority: Priority = Priority.MEDIUM, block: suspend CombatAttack.(NPC) -> Unit) {
    onNPC<CombatAttack>({ wildcardEquals(npc, it.id) }, priority, block)
}

fun characterCombatAttack(priority: Priority = Priority.MEDIUM, block: suspend CombatAttack.(Character) -> Unit) {
    onCharacter<CombatAttack>(priority = priority, block = block)
}

fun characterSpellAttack(spell: String = "*", priority: Priority = Priority.MEDIUM, block: suspend CombatAttack.(Character) -> Unit) {
    onCharacter<CombatAttack>({ damage > 0 && type == "magic" && wildcardEquals(spell, this.spell) }, priority, block)
}

fun characterSpellAttack(spells: Set<String>, priority: Priority = Priority.MEDIUM, block: suspend CombatAttack.(Character) -> Unit) {
    if (spells.any { it.contains("*") || it.contains("#") }) {
        throw IllegalArgumentException("Spell collections cannot contain wildcards.")
    }
    onCharacter<CombatAttack>({ damage > 0 && type == "magic" && spells.contains(spell) }, priority, block)
}

fun block(vararg weapons: String, priority: Priority = Priority.MEDIUM, block: suspend CombatAttack.(Character) -> Unit) {
    for (weapon in weapons) {
        onCharacter<CombatAttack>({ !blocked && wildcardEquals(weapon, target.weapon.id) }, priority) { character ->
            block.invoke(this, character)
        }
    }
}

fun block(weapon: String, priority: Priority = Priority.MEDIUM, block: suspend CombatAttack.(Character) -> Unit) {
    onCharacter<CombatAttack>({ !blocked && wildcardEquals(weapon, target.weapon.id) }, priority) { character ->
        block.invoke(this, character)
    }
}

fun block(priority: Priority = Priority.MEDIUM, block: suspend CombatAttack.(Character) -> Unit) {
    onCharacter<CombatAttack>({ !blocked }, priority, block)
}