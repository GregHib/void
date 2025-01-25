package content.entity.combat.hit

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

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
) : Event {

    override val notification: Boolean = true

    override val size = 5

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_combat_hit"
        1 -> dispatcher.identifier
        2 -> weapon.id
        3 -> type
        4 -> spell
        else -> null
    }
}

fun combatHit(weapon: String = "*", type: String = "*", spell: String = "*", handler: suspend CombatHit.(Player) -> Unit) {
    Events.handle("player_combat_hit", "player", weapon, type, spell, handler = handler)
}

fun npcCombatHit(npc: String = "*", weapon: String = "*", type: String = "*", spell: String = "*", handler: suspend CombatHit.(NPC) -> Unit) {
    Events.handle("npc_combat_hit", npc, weapon, type, spell, handler = handler)
}

fun characterCombatHit(weapon: String = "*", type: String = "*", spell: String = "*", handler: suspend CombatHit.(Character) -> Unit) {
    combatHit(weapon, type, spell, handler)
    npcCombatHit("*", weapon, type, spell, handler)
}