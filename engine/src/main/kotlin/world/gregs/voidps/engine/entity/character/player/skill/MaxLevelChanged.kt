package world.gregs.voidps.engine.entity.character.player.skill

import world.gregs.voidps.engine.event.Event

/**
 * Notification when a skills max level changes
 * @see [CurrentLevelChanged]
 */
data class MaxLevelChanged(val skill: Skill, val from: Int, val to: Int) : Event