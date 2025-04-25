package world.gregs.voidps.engine.client.update.npc

import world.gregs.voidps.engine.client.update.CharacterTask
import world.gregs.voidps.engine.client.update.iterator.TaskIterator
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs

/**
 * Resets non-persistent changes
 */
class NPCResetTask(
    iterator: TaskIterator<NPC>,
    override val characters: NPCs
) : CharacterTask<NPC>(iterator) {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun run(npc: NPC) {
        npc.visuals.reset()
        npc.steps.follow = npc.steps.previous
    }

}