package world.gregs.voidps.engine.entity.character.player.skill.exp

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on

data class GrantExp(
    val skill: Skill,
    val from: Double,
    val to: Double
) : Event

fun experience(filter: GrantExp.(Player) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend GrantExp.(Player) -> Unit) {
    on<GrantExp>(filter, priority, block)
}

fun experience(skill: Skill? = null, block: suspend GrantExp.(Player) -> Unit) {
    on<GrantExp>({ skill == null || this.skill == skill }) { player: Player ->
        block.invoke(this, player)
    }
}