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

fun maxLevelChange(skills: Set<Skill>, priority: Priority = Priority.MEDIUM, block: suspend MaxLevelChanged.(Player) -> Unit) {
    on<MaxLevelChanged>({ skills.contains(skill) }, priority, block)
}

fun maxLevelUp(skill: Skill? = null, block: suspend MaxLevelChanged.(Player) -> Unit) {
    on<MaxLevelChanged>({ (skill == null || skill == this.skill) && to > from }, block = block)
}