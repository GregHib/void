package world.gregs.voidps.engine.entity.character.player.skill.exp

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.on

data class GrantExp(
    val skill: Skill,
    val from: Double,
    val to: Double
) : Event

fun experience(skill: Skill? = null, block: suspend GrantExp.(Player) -> Unit) {
    on<GrantExp>({ skill == null || this.skill == skill }) { player ->
        block.invoke(this, player)
    }
}