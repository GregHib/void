package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event

/**
 * Modify max hit damage
 */
data class HitDamageModifier(
    val target: Character?,
    val skill: Skill,
    val strengthBonus: Int,
    var damage: Double,
    val weapon: Item?
) : Event