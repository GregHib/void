package world.gregs.voidps.engine.client.update.task.npc

import world.gregs.voidps.engine.client.update.task.CharacterTask
import world.gregs.voidps.engine.client.update.task.TaskIterator
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.update.LocalChange
import world.gregs.voidps.engine.map.Delta

class NPCChangeTask(
    iterator: TaskIterator<NPC>,
    override val characters: NPCs
) : CharacterTask<NPC>(iterator) {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun run(npc: NPC) {
        val movement = npc.movement
        val delta = movement.delta

        npc.change = when {
            delta != Delta.EMPTY && movement.walkStep != Direction.NONE && npc.def["crawl", false] -> LocalChange.Crawl
            delta != Delta.EMPTY && movement.runStep != Direction.NONE -> LocalChange.Run
            delta != Delta.EMPTY && movement.walkStep != Direction.NONE -> LocalChange.Walk
            delta != Delta.EMPTY -> LocalChange.Tele
            npc.visuals.flag != 0 -> LocalChange.Update
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