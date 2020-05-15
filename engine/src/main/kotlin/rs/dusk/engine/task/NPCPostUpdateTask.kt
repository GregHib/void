package rs.dusk.engine.task

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import rs.dusk.engine.EntityTask
import rs.dusk.engine.entity.list.npc.NPCs
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.index.npc.NPC
import rs.dusk.engine.model.entity.index.update.visual.getAnimation
import rs.dusk.engine.model.entity.index.update.visual.getGraphic
import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class NPCPostUpdateTask(override val entities: NPCs) : EntityTask<NPC>() {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun runAsync(npc: NPC) = GlobalScope.async<Unit> {
        npc.movement.delta = Tile(0)
        npc.movement.walkStep = Direction.NONE
        npc.movement.runStep = Direction.NONE
        npc.getAnimation().apply {
            first = -1
            second = -1
            third = -1
            fourth = -1
            speed = 0
        }
        repeat(4) {
            npc.getGraphic(it).apply {
                id = -1
                delay = 0
                height = 0
                rotation = 0
                forceRefresh = false
            }
        }
    }

}