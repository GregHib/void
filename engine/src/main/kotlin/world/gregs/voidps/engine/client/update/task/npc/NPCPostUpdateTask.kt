package world.gregs.voidps.engine.client.update.task.npc

import world.gregs.voidps.engine.client.update.task.CharacterTask
import world.gregs.voidps.engine.client.update.task.TaskIterator
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs

/**
 * Resets non-persistent changes
 */
class NPCPostUpdateTask(
    iterator: TaskIterator<NPC>,
    override val characters: NPCs
) : CharacterTask<NPC>(iterator) {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun run(npc: NPC) {
        npc.movement.reset()
        npc.visuals.reset()
    }

}