package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event

/**
 * Modify the potential maximum hit
 * @param type the combat type, typically: melee, range or magic
 * @param damage the maximum hit possible on [target] using [weapon]
 */
data class HitDamageModifier(
    val target: Character?,
    val type: String,
    val strengthBonus: Int,
    var damage: Double,
    val weapon: Item?
) : Event