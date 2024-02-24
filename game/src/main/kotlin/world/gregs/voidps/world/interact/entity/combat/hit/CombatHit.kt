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
import world.gregs.voidps.world.interact.entity.player.combat.prayer.praying

/**
 * Damage done by [source] to the emitter
 * Used for hit graphics, for effects use [CombatAttack]
 * @param type the combat type, typically: melee, range or magic
 * @param damage the damage inflicted by the [source]
 * @param weapon weapon used
 * @param spell magic spell used
 * @param special whether weapon special attack was used
 */
data class CombatHit(
    val source: Character,
    val type: String,
    val damage: Int,
    val weapon: Item,
    val spell: String,
    val special: Boolean
) : Event

fun combatHit(block: suspend CombatHit.(Player) -> Unit) {
    on<CombatHit>(block = block)
}

fun npcCombatHit(block: suspend CombatHit.(NPC) -> Unit) {
    onNPC<CombatHit>(block = block)
}

fun characterCombatHit(block: suspend CombatHit.(Character) -> Unit) {
    onCharacter<CombatHit>(block = block)
}

fun weaponHit(weapon: String = "*", type: String = "*", priority: Priority = Priority.MEDIUM, block: suspend CombatHit.(Character) -> Unit) {
    onCharacter<CombatHit>({ wildcardEquals(weapon, this.weapon.id) && wildcardEquals(type, this.type) }, priority, block)
}

fun specialAttackHit(weapon: String = "*", type: String = "*", priority: Priority = Priority.MEDIUM, block: suspend CombatHit.(Character) -> Unit) {
    onCharacter<CombatHit>({ special && wildcardEquals(weapon, this.weapon.id) && wildcardEquals(type, this.type) }, priority, block)
}

fun specialAttackHit(vararg weapons: String, type: String = "*", priority: Priority = Priority.MEDIUM, block: suspend CombatHit.(Character) -> Unit) {
    for (weapon in weapons) {
        onCharacter<CombatHit>({ special && wildcardEquals(weapon, this.weapon.id) && wildcardEquals(type, this.type) }, priority, block)
    }
}

fun prayerHit(prayer: String, priority: Priority = Priority.MEDIUM, block: suspend CombatHit.(Character) -> Unit) {
    onCharacter<CombatHit>({ source.praying(prayer) }, priority, block)
}