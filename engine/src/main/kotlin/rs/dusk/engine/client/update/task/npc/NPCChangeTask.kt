package rs.dusk.engine.client.update.task.npc

import rs.dusk.engine.entity.Direction
import rs.dusk.engine.entity.character.npc.NPC
import rs.dusk.engine.entity.character.npc.NPCMoveType
import rs.dusk.engine.entity.character.npc.NPCs
import rs.dusk.engine.entity.character.update.LocalChange
import rs.dusk.engine.event.Priority.NPC_CHANGE
import rs.dusk.engine.tick.task.EntityTask

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 15, 2020
 */
class NPCChangeTask(override val entities: NPCs) : EntityTask<NPC>(NPC_CHANGE) {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun runAsync(npc: NPC) {
        val movement = npc.movement
        val delta = movement.delta

        npc.change = when {
            delta.id != 0 && movement.walkStep != Direction.NONE && npc.movementType == NPCMoveType.Crawl -> LocalChange.Crawl
            delta.id != 0 && movement.runStep != Direction.NONE -> LocalChange.Run
            delta.id != 0 && movement.walkStep != Direction.NONE -> LocalChange.Walk
            delta.id != 0 -> LocalChange.Tele
            npc.visuals.update != null -> LocalChange.Update
            else -> null
        }

        if (npc.change == LocalChange.Run || npc.change == LocalChange.Walk || npc.change == LocalChange.Crawl) {
            npc.walkDirection = Direction.clockwise.indexOf(movement.walkStep)
        }
        if (npc.change == LocalChange.Run) {
            npc.runDirection = Direction.clockwise.indexOf(movement.runStep)
        }
    }

}