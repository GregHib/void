package world.gregs.voidps.engine.entity.character.player.skill.level

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on

/**
 * Notification when a skills max level changes
 * @see [CurrentLevelChanged]
 */
data class MaxLevelChanged(val skill: Skill, val from: Int, val to: Int) : Event

fun maxLevelChange(filter: MaxLevelChanged.(Player) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend MaxLevelChanged.(Player) -> Unit) {
    on<MaxLevelChanged>(filter, priority, block)
}