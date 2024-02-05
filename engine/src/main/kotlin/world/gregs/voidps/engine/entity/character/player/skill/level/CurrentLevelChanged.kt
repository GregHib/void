package world.gregs.voidps.engine.entity.character.player.skill.level

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on

/**
 * Notification when current skill level has changed.
 * @see [MaxLevelChanged]
 */
data class CurrentLevelChanged(val skill: Skill, val from: Int, val to: Int) : CancellableEvent()

@JvmName("levelChangePlayer")
fun levelChange(filter: CurrentLevelChanged.(Player) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend CurrentLevelChanged.(Player) -> Unit) {
    on<CurrentLevelChanged>(filter, priority, block)
}

@JvmName("levelChangeNPC")
fun levelChange(filter: CurrentLevelChanged.(NPC) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend CurrentLevelChanged.(NPC) -> Unit) {
    on<CurrentLevelChanged>(filter, priority, block)
}

@JvmName("levelChangeCharacter")
fun levelChange(filter: CurrentLevelChanged.(Character) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend CurrentLevelChanged.(Character) -> Unit) {
    on<CurrentLevelChanged>(filter, priority, block)
}

fun levelChange(skill: Skill, block: suspend CurrentLevelChanged.(Player) -> Unit) {
    on<CurrentLevelChanged>({ this.skill == skill }) { player: Player ->
        block.invoke(this, player)
    }
}

fun characterLevelChange(skill: Skill, block: suspend CurrentLevelChanged.(Character) -> Unit) {
    on<CurrentLevelChanged>({ this.skill == skill }) { player: Character ->
        block.invoke(this, player)
    }
}