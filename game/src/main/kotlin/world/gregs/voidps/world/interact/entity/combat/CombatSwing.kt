package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.*
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.spell
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack

/**
 * A turn in a combat scenario resulting one or many hits and a [delay] until the next turn
 */
class CombatSwing(
    val target: Character
) : CancellableEvent() {
    var delay: Int? = null
    var priority: Priority = Priority.HIGHEST

    fun swung(): Boolean {
        return delay != null
    }

    override fun size() = 7

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_combat_swing"
        1 -> dispatcher.identifier
        2 -> if (dispatcher is Character) dispatcher.weapon.id else ""
        3 -> if (dispatcher is Character) dispatcher.fightStyle else "melee"
        4 -> if (dispatcher is Character) dispatcher.spell else ""
        5 -> delay != null
        6 -> priority
        else -> null
    }
}

fun combatSwing(
    weapon: String = "*",
    type: String = "*",
    spell: String = "*",
    priority: Priority = Priority.MEDIUM,
    swung: Boolean? = false,
    override: Boolean = true,
    block: suspend CombatSwing.(Player) -> Unit
) {
    Events.handle("player_combat_swing", "player", weapon, type, spell, swung, priority, override = override, handler = block)
}

fun npcCombatSwing(
    npc: String = "*",
    weapon: String = "*",
    type: String = "*",
    spell: String = "*",
    priority: Priority = Priority.MEDIUM,
    swung: Boolean? = false,
    override: Boolean = true,
    block: suspend CombatSwing.(NPC) -> Unit
) {
    Events.handle("npc_combat_swing", npc, weapon, type, spell, swung, priority, override = override, handler = block)
}

fun characterCombatSwing(
    weapon: String = "*",
    type: String = "*",
    spell: String = "*",
    priority: Priority = Priority.MEDIUM,
    swung: Boolean? = false,
    override: Boolean = true,
    block: suspend CombatSwing.(Character) -> Unit
) {
    combatSwing(weapon, type, spell, priority, swung, override, block)
    npcCombatSwing("*", weapon, type, spell, priority, swung, override, block)
}

fun spellSwing(spell: String = "*", priority: Priority = Priority.MEDIUM, block: suspend CombatSwing.(Player) -> Unit) {
    on<CombatSwing>({ player -> !swung() && player.fightStyle == "magic" && player.spell.isNotBlank() && wildcardEquals(spell, player.spell) }, priority) { character ->
        block.invoke(this, character)
    }
}

fun weaponSwing(weapon: String = "*", priority: Priority = Priority.MEDIUM, block: suspend CombatSwing.(Player) -> Unit) {
    on<CombatSwing>({ player -> !swung() && !player.specialAttack && wildcardEquals(weapon, player.weapon.id) }, priority) { character ->
        block.invoke(this, character)
    }
}

fun weaponSwing(vararg weapons: String = arrayOf("*"), style: String = "*", priority: Priority = Priority.MEDIUM, block: suspend CombatSwing.(Player) -> Unit) {
    for (weapon in weapons) {
        on<CombatSwing>({ player -> !swung() && !player.specialAttack && wildcardEquals(style, player.fightStyle) && wildcardEquals(weapon, player.weapon.id) }, priority) { character ->
            block.invoke(this, character)
        }
    }
}

fun characterSpellSwing(spell: String = "*", priority: Priority = Priority.MEDIUM, block: suspend CombatSwing.(Character) -> Unit) {
    onCharacter<CombatSwing>({ char -> !swung() && char.fightStyle == "magic" && char.spell.isNotBlank() && wildcardEquals(spell, char.spell) }, priority) { character ->
        block.invoke(this, character)
    }
}

fun characterSpellSwing(spells: Set<String>, priority: Priority = Priority.MEDIUM, block: suspend CombatSwing.(Character) -> Unit) {
    if (spells.any { it.contains("*") || it.contains("#") }) {
        throw IllegalArgumentException("Spell collections cannot contain wildcards.")
    }
    onCharacter<CombatSwing>({ player -> !swung() && spells.contains(player.spell) }, priority) { character ->
        block.invoke(this, character)
    }
}

fun spellSwing(spells: Set<String>, priority: Priority = Priority.MEDIUM, block: suspend CombatSwing.(Player) -> Unit) {
    if (spells.any { it.contains("*") || it.contains("#") }) {
        throw IllegalArgumentException("Spell collections cannot contain wildcards.")
    }
    on<CombatSwing>({ player -> !swung() && spells.contains(player.spell) }, priority) { character ->
        block.invoke(this, character)
    }
}

fun specialAttackSwing(vararg weapons: String = arrayOf("*"), style: String = "melee", priority: Priority = Priority.MEDIUM, block: suspend CombatSwing.(Player) -> Unit) {
    for (weapon in weapons) {
        on<CombatSwing>({ player -> !swung() && player.specialAttack && player.fightStyle == style && wildcardEquals(weapon, player.weapon.id) }, priority) { character ->
            block.invoke(this, character)
        }
    }
}