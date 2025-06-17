package world.gregs.voidps.engine.entity.character.player.skill.level

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

/**
 * Notification when a skills max level changes
 * @see [CurrentLevelChanged]
 */
data class MaxLevelChanged(val skill: Skill, val from: Int, val to: Int) : Event {

    override val notification = true

    override val size = 5

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_max_level_change"
        1 -> skill
        2 -> dispatcher.identifier
        3 -> from
        4 -> to
        else -> null
    }
}

fun maxLevelChange(vararg skills: Skill, from: Int? = null, to: Int? = null, handler: suspend MaxLevelChanged.(Player) -> Unit) {
    if (skills.isEmpty()) {
        Events.handle("player_max_level_change", "*", "player", from ?: "*", to ?: "*", handler = handler)
    } else {
        for (skill in skills) {
            Events.handle("player_max_level_change", skill, "player", from ?: "*", to ?: "*", handler = handler)
        }
    }
}

fun npcMaxLevelChange(npc: String = "*", vararg skills: Skill, from: Int? = null, to: Int? = null, handler: suspend MaxLevelChanged.(NPC) -> Unit) {
    if (skills.isEmpty()) {
        Events.handle("npc_max_level_change", "*", npc, from ?: "*", to ?: "*", handler = handler)
    } else {
        for (skill in skills) {
            Events.handle("npc_max_level_change", skill, npc, from ?: "*", to ?: "*", handler = handler)
        }
    }
}
