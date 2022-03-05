package world.gregs.voidps.engine.entity.character.npc

import it.unimi.dsi.fastutil.ints.IntArrayList
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.Delta

class NPCTrackingSet {

    val locals = IntArrayList(LOCAL_NPC_CAP)

    fun clear() {
        locals.clear()
    }

    companion object {
        const val LOCAL_NPC_CAP = 255
    }
}

val NPC.teleporting: Boolean
    get() = movement.delta != Delta.EMPTY && movement.walkStep == Direction.NONE && movement.runStep == Direction.NONE
