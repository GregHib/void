package world.gregs.voidps.world.interact.entity.combat.hit

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.world.interact.entity.combat.*


/**
 * Hits player without interrupting them
 */
fun Character.directHit(damage: Int, type: String = "damage", weapon: Item = Item.EMPTY, spell: String = "", special: Boolean = false, source: Character = this) =
    directHit(source, damage, type, weapon, spell, special)

/**
 * Hits player without interrupting them
 */
fun Character.directHit(source: Character, damage: Int, type: String = "damage", weapon: Item = Item.EMPTY, spell: String = "", special: Boolean = false) {
    if (source.dead) {
        return
    }
    emit(CombatHit(source, type, damage, weapon, spell, special))
    if (source["debug", false] || this["debug", false]) {
        val player = if (this["debug", false] && this is Player) this else source as Player
        val message = "Damage: $damage ($type, ${if (weapon.isEmpty()) "unarmed" else weapon.id})"
        player.message(message)
    }
}