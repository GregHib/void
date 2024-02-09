package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

/**
 * A turn in a combat scenario resulting one or many hits and a [delay] until the next turn
 */
class CombatSwing(
    val target: Character
) : CancellableEvent() {
    var delay: Int? = null

    fun swung(): Boolean {
        return delay != null
    }

}

fun combatSwing(priority: Priority = Priority.MEDIUM, block: suspend CombatSwing.(Player) -> Unit) {
    on<CombatSwing>(priority = priority, block = block)
}

fun npcSwing(npc: String = "*", priority: Priority = Priority.MEDIUM, block: suspend CombatSwing.(NPC) -> Unit) {
    if (npc == "*") {
        on<CombatSwing>({ !swung() }, priority) { character: NPC ->
            block.invoke(this, character)
        }
    } else {
        on<CombatSwing>({ !swung() && wildcardEquals(npc, it.id) }, priority) { character: NPC ->
            block.invoke(this, character)
        }
    }
}

fun spellSwing(spell: String = "*", priority: Priority = Priority.MEDIUM, block: suspend CombatSwing.(Player) -> Unit) {
    on<CombatSwing>({ player -> !swung() && player.fightStyle == "magic" && player.spell.isNotBlank() && wildcardEquals(spell, player.spell) }, priority) { character: Player ->
        block.invoke(this, character)
    }
}

fun weaponSwing(weapon: String = "*", priority: Priority = Priority.MEDIUM, block: suspend CombatSwing.(Player) -> Unit) {
    on<CombatSwing>({ player -> !swung() && !player.specialAttack && wildcardEquals(weapon, player.weapon.id) }, priority) { character: Player ->
        block.invoke(this, character)
    }
}

fun weaponSwing(vararg weapons: String = arrayOf("*"), style: String = "*", priority: Priority = Priority.MEDIUM, block: suspend CombatSwing.(Player) -> Unit) {
    for (weapon in weapons) {
        on<CombatSwing>({ player -> !swung() && !player.specialAttack && wildcardEquals(style, player.fightStyle) && wildcardEquals(weapon, player.weapon.id) }, priority) { character: Player ->
            block.invoke(this, character)
        }
    }
}

fun characterSpellSwing(spell: String = "*", priority: Priority = Priority.MEDIUM, block: suspend CombatSwing.(Character) -> Unit) {
    on<CombatSwing>({ char -> !swung() && char.fightStyle == "magic" && char.spell.isNotBlank() && wildcardEquals(spell, char.spell) }, priority) { character: Character ->
        block.invoke(this, character)
    }
}

fun characterSpellSwing(spells: Set<String>, priority: Priority = Priority.MEDIUM, block: suspend CombatSwing.(Character) -> Unit) {
    if (spells.any { it.contains("*") || it.contains("#") }) {
        throw IllegalArgumentException("Spell collections cannot contain wildcards.")
    }
    on<CombatSwing>({ player -> !swung() && spells.contains(player.spell) }, priority) { character: Character ->
        block.invoke(this, character)
    }
}

fun spellSwing(spells: Set<String>, priority: Priority = Priority.MEDIUM, block: suspend CombatSwing.(Player) -> Unit) {
    if (spells.any { it.contains("*") || it.contains("#") }) {
        throw IllegalArgumentException("Spell collections cannot contain wildcards.")
    }
    on<CombatSwing>({ player -> !swung() && spells.contains(player.spell) }, priority) { character: Player ->
        block.invoke(this, character)
    }
}

fun specialAttackSwing(vararg weapons: String = arrayOf("*"), style: String = "melee", priority: Priority = Priority.MEDIUM, block: suspend CombatSwing.(Player) -> Unit) {
    for (weapon in weapons) {
        on<CombatSwing>({ player -> !swung() && player.specialAttack && player.fightStyle == style && wildcardEquals(weapon, player.weapon.id) }, priority) { character: Player ->
            block.invoke(this, character)
        }
    }
}