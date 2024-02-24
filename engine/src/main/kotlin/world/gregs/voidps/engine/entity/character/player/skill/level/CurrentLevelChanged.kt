package world.gregs.voidps.engine.entity.character.player.skill.level

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.onCharacter
import world.gregs.voidps.engine.event.onNPC
import world.gregs.voidps.engine.event.wildcardEquals

/**
 * Notification when current skill level has changed.
 * @see [MaxLevelChanged]
 */
data class CurrentLevelChanged(val skill: Skill, val from: Int, val to: Int) : CancellableEvent()

fun levelChange(skill: Skill? = null, block: suspend CurrentLevelChanged.(Player) -> Unit) {
    on<CurrentLevelChanged>({ skill == null || this.skill == skill }) { player: Player ->
        block.invoke(this, player)
    }
}

fun npcLevelChange(npc: String = "*", skill: Skill, priority: Priority = Priority.MEDIUM, block: suspend CurrentLevelChanged.(NPC) -> Unit) {
    onNPC<CurrentLevelChanged>({ wildcardEquals(npc, it.id) && skill == this.skill }, priority, block)
}

fun characterLevelChange(skill: Skill, block: suspend CurrentLevelChanged.(Character) -> Unit) {
    onCharacter<CurrentLevelChanged>({ skill == this.skill }, block = block)
}