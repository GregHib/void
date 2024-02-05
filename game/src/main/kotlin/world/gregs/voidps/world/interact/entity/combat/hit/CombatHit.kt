package world.gregs.voidps.world.interact.entity.combat.hit

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on

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


@JvmName("combatHitPlayer")
fun combatHit(filter: CombatHit.(Player) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, block: suspend CombatHit.(Player) -> Unit) {
    on<CombatHit>(filter, priority, block)
}

@JvmName("combatHitNPC")
fun combatHit(filter: CombatHit.(NPC) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, block: suspend CombatHit.(NPC) -> Unit) {
    on<CombatHit>(filter, priority, block)
}

@JvmName("combatHitCharacter")
fun combatHit(filter: CombatHit.(Character) -> Boolean = { true }, priority: Priority = Priority.MEDIUM, block: suspend CombatHit.(Character) -> Unit) {
    on<CombatHit>(filter, priority, block)
}