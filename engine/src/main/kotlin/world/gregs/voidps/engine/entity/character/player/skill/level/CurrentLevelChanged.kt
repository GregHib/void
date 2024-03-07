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

fun levelChange(skill: Skill? = null, from: Int? = null, to: Int? = null, override: Boolean = true, block: suspend CurrentLevelChanged.(Player) -> Unit) {
    Events.handle("player_level_change", skill ?: "*", "player", from ?: "*", to ?: "*", override = override, handler = block)
}

fun npcLevelChange(npc: String = "*", skill: Skill? = null, from: Int? = null, to: Int? = null, override: Boolean = true, block: suspend CurrentLevelChanged.(NPC) -> Unit) {
    Events.handle("npc_level_change", skill ?: "*", npc, from ?: "*", to ?: "*", override = override, handler = block)
}

fun characterLevelChange(skill: Skill? = null, from: Int? = null, to: Int? = null, override: Boolean = true, block: suspend CurrentLevelChanged.(Character) -> Unit) {
    Events.handle("player_level_change", skill ?: "*", "player", from ?: "*", to ?: "*", override = override, handler = block)
    Events.handle("npc_level_change", skill ?: "*", "*", from ?: "*", to ?: "*", override = override, handler = block)
}