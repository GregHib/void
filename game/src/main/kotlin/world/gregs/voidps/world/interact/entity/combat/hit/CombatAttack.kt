package world.gregs.voidps.world.interact.entity.combat.hit

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
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

    override val notification: Boolean = true

    override val size = 5

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_combat_attack"
        1 -> dispatcher.identifier
        2 -> weapon.id
        3 -> type
        4 -> spell
        else -> null
    }
}

fun combatAttack(weapon: String = "*", type: String = "*", spell: String = "*", handler: suspend CombatAttack.(Player) -> Unit) {
    Events.handle("player_combat_attack", "player", weapon, type, spell, handler = handler)
}

fun npcCombatAttack(npc: String = "*", weapon: String = "*", type: String = "*", spell: String = "*", handler: suspend CombatAttack.(NPC) -> Unit) {
    Events.handle("npc_combat_attack", npc, weapon, type, spell, handler = handler)
}

fun characterCombatAttack(weapon: String = "*", type: String = "*", spell: String = "*", handler: suspend CombatAttack.(Character) -> Unit) {
    combatAttack(weapon, type, spell, handler)
    npcCombatAttack("*", weapon, type, spell, handler)
}