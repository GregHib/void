package world.gregs.voidps.engine.entity.character.player.skill.level

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

/**
 * Notification when current skill level has changed.
 * @see [MaxLevelChanged]
 */
data class CurrentLevelChanged(val skill: Skill, val from: Int, val to: Int) : CancellableEvent() {

    override val notification = true

    override val size = 5

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "${dispatcher.key}_level_change"
        1 -> skill
        2 -> dispatcher.identifier
        3 -> from
        4 -> to
        else -> null
    }
}

fun levelChange(skill: Skill? = null, from: Int? = null, to: Int? = null, handler: suspend CurrentLevelChanged.(Player) -> Unit) {
    Events.handle("player_level_change", skill ?: "*", "player", from ?: "*", to ?: "*", handler = handler)
}

fun npcLevelChange(npc: String = "*", skill: Skill? = null, from: Int? = null, to: Int? = null, handler: suspend CurrentLevelChanged.(NPC) -> Unit) {
    Events.handle("npc_level_change", skill ?: "*", npc, from ?: "*", to ?: "*", handler = handler)
}

fun characterLevelChange(skill: Skill? = null, from: Int? = null, to: Int? = null, handler: suspend CurrentLevelChanged.(Character) -> Unit) {
    levelChange(skill, from, to, handler)
    npcLevelChange("*", skill, from, to, handler)
}
