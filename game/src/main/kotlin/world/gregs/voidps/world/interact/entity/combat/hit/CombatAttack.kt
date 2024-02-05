package world.gregs.voidps.world.interact.entity.combat.hit

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
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

@JvmName("combatAttackPlayer")
fun combatAttack(filter: CombatAttack.(Player) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, block: suspend CombatAttack.(Player) -> Unit) {
    on<CombatAttack>(filter, priority, block)
}

@JvmName("combatAttackNPC")
fun combatAttack(filter: CombatAttack.(NPC) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, block: suspend CombatAttack.(NPC) -> Unit) {
    on<CombatAttack>(filter, priority, block)
}

@JvmName("combatAttackCharacter")
fun combatAttack(filter: CombatAttack.(Character) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, block: suspend CombatAttack.(Character) -> Unit) {
    on<CombatAttack>(filter, priority, block)
}

fun block(weapon: String = "*", block: suspend CombatAttack.(Character) -> Unit) {
    on<CombatAttack>({ !blocked && wildcardEquals(weapon, target.weapon.id) }) { character: Character ->
        block.invoke(this, character)
    }
}