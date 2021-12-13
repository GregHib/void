package world.gregs.voidps.engine.client.update.task.npc

import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.path.PathFinder
import world.gregs.voidps.engine.tick.task.EntityTask

/**
 * Calculates paths for npcs that want to move
 */
class NPCPathTask(override val entities: NPCs, val finder: PathFinder) : EntityTask<NPC>() {

    override fun predicate(entity: NPC): Boolean {
        return entity.movement.path.state == Path.State.Waiting
    }

    override fun runAsync(npc: NPC) {
        val path = npc.movement.path
        path.result = finder.find(npc, path, path.smart)
    }

}