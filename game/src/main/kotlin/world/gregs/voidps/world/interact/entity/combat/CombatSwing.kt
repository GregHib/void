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

@JvmName("combatSwingPlayer")
fun combatSwing(filter: CombatSwing.(Player) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, block: suspend CombatSwing.(Player) -> Unit) {
    on<CombatSwing>(filter, priority, block)
}

@JvmName("combatSwingNPC")
fun combatSwing(filter: CombatSwing.(NPC) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, block: suspend CombatSwing.(NPC) -> Unit) {
    on<CombatSwing>(filter, priority, block)
}

@JvmName("combatSwingCharacter")
fun combatSwing(filter: CombatSwing.(Character) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, block: suspend CombatSwing.(Character) -> Unit) {
    on<CombatSwing>(filter, priority, block)
}

fun npcSwing(npc: String = "*", priority: Priority = Priority.MEDIUM, block: suspend CombatSwing.(NPC) -> Unit) {
    if (npc == "*") {
        combatSwing({ !swung() }, priority) { character: NPC ->
            block.invoke(this, character)
        }
    } else {
        on<CombatSwing>({ !swung() && wildcardEquals(npc, it.id) }, priority) { character: NPC ->
            block.invoke(this, character)
        }
    }
}

fun spellSwing(spell: String = "*", priority: Priority = Priority.MEDIUM, block: suspend CombatSwing.(Player) -> Unit) {
    on<CombatSwing>({ player -> !swung() && player.fightStyle == "magic" && wildcardEquals(spell, player.spell) }, priority) { character: Player ->
        block.invoke(this, character)
    }
}

fun spellSwing(spells: Set<String>, priority: Priority = Priority.MEDIUM, block: suspend CombatSwing.(Player) -> Unit) {
    if (spells.any { it.contains("*") || it.contains("#") }) {
        throw IllegalArgumentException("Spell collections cannot contain wildcards.")
    }
    combatSwing({ player -> !swung() && spells.contains(player.spell) }, priority) { character: Player ->
        block.invoke(this, character)
    }
}

fun specialAttackSwing(style: String = "melee", weapon: String = "*", priority: Priority = Priority.MEDIUM, block: suspend CombatSwing.(Player) -> Unit) {
    on<CombatSwing>({ player -> !swung() && player.specialAttack && player.fightStyle == style && wildcardEquals(weapon, player.weapon.id) }, priority) { character: Player ->
        block.invoke(this, character)
    }
}

fun specialAttackSwing(style: String, weapons: Set<String>, priority: Priority = Priority.MEDIUM, block: suspend CombatSwing.(Player) -> Unit) {
    if (weapons.any { it.contains("*") || it.contains("#") }) {
        throw IllegalArgumentException("Weapon collections cannot contain wildcards.")
    }
    combatSwing({ player -> !swung() && player.specialAttack && player.fightStyle == style && weapons.contains(player.weapon.id) }, priority) { character: Player ->
        block.invoke(this, character)
    }
}