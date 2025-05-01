package content.entity.combat.hit

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.network.login.protocol.visual.update.HitSplat

/**
 * Damage done by [source] to the emitter
 * Used for defend graphics, for effects use [CombatAttack]
 * @param type the combat type, typically: melee, range or magic
 * @param mark the hit-splat type
 * @param damage the damage inflicted by the [source]
 * @param weapon weapon used
 * @param spell magic spell used
 * @param special whether weapon special attack was used
 */
data class CombatDamage(
    val source: Character,
    val type: String,
    val mark: HitSplat.Mark,
    val damage: Int,
    val weapon: Item,
    val spell: String,
    val special: Boolean
) : Event {

    override val notification: Boolean = true

    override val size = 5

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_combat_damage"
        1 -> dispatcher.identifier
        2 -> weapon.id
        3 -> type
        4 -> spell
        else -> null
    }
}

fun combatDamage(weapon: String = "*", type: String = "*", spell: String = "*", handler: suspend CombatDamage.(Player) -> Unit) {
    Events.handle("player_combat_damage", "player", weapon, type, spell, handler = handler)
}

fun npcCombatDamage(npc: String = "*", weapon: String = "*", type: String = "*", spell: String = "*", handler: suspend CombatDamage.(NPC) -> Unit) {
    Events.handle("npc_combat_damage", npc, weapon, type, spell, handler = handler)
}

fun characterCombatDamage(weapon: String = "*", type: String = "*", spell: String = "*", handler: suspend CombatDamage.(Character) -> Unit) {
    combatDamage(weapon, type, spell, handler)
    npcCombatDamage("*", weapon, type, spell, handler)
}