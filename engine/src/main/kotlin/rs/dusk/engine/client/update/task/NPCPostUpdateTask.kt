package rs.dusk.engine.client.update.task

import rs.dusk.engine.model.engine.task.EntityTask
import rs.dusk.engine.model.entity.index.npc.NPC
import rs.dusk.engine.model.entity.index.npc.NPCs

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class NPCPostUpdateTask(override val entities: NPCs) : EntityTask<NPC>() {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun runAsync(npc: NPC) {
        npc.movement.reset()
        npc.visuals.aspects.forEach { (_, visual) ->
            visual.reset(npc)
        }
    }

}