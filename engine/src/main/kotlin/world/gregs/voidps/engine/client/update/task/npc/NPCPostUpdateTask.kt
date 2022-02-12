package world.gregs.voidps.engine.client.update.task.npc

import world.gregs.voidps.engine.client.update.task.SequentialTask
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs

/**
 * Resets non-persistent changes
 */
class NPCPostUpdateTask(override val characters: NPCs) : SequentialTask<NPC>() {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun run(npc: NPC) {
        npc.movement.reset()
        npc.visuals.reset(npc)
    }

}