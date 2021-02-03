package world.gregs.voidps.engine.client.update.task.npc

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.event.Priority.NPC_UPDATE_FINISHED
import world.gregs.voidps.engine.tick.task.EntityTask

/**
 * Resets non-persistent changes
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
class NPCPostUpdateTask(override val entities: NPCs) : EntityTask<NPC>(NPC_UPDATE_FINISHED) {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun runAsync(npc: NPC) {
        npc.movement.reset()
        npc.visuals.reset(npc)
    }

}