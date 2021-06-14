package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Event

/**
 * Hit chance modifier
 * @param offense whether calculating the attacker or defender chance
 */
data class HitChanceModifier(
    val target: Character?,
    val skill: Skill,
    val offense: Boolean,
    var chance: Double,
    val weapon: Item?
) : Event