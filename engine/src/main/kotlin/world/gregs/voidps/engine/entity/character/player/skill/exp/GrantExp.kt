package world.gregs.voidps.engine.entity.character.player.skill.exp

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class GrantExp(
    val skill: Skill,
    val from: Double,
    val to: Double,
) : Event {

    override val size = 2

    override fun parameter(dispatcher: EventDispatcher, index: Int): Any? = when (index) {
        0 -> "grant_experience"
        1 -> skill
        else -> null
    }
}

fun experience(skill: Skill? = null, handler: suspend GrantExp.(Player) -> Unit) {
    Events.handle("grant_experience", skill ?: "*", handler = handler)
}
