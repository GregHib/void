package world.gregs.voidps.engine.entity.character.player.skill.level

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.engine.event.on

/**
 * Notification when a skills max level changes
 * @see [CurrentLevelChanged]
 */
data class MaxLevelChanged(val skill: Skill, val from: Int, val to: Int) : Event

fun maxLevelChange(skill: Skill, block: suspend MaxLevelChanged.(Player) -> Unit) {
    on<MaxLevelChanged>({ this.skill == skill }) { player: Player ->
        block.invoke(this, player)
    }
}

fun characterMaxLevelChange(skill: Skill, block: suspend MaxLevelChanged.(Character) -> Unit) {
    on<MaxLevelChanged>({ this.skill == skill }) { player: Character ->
        block.invoke(this, player)
    }
}