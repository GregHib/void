package world.gregs.voidps.engine.entity.character.npc

import it.unimi.dsi.fastutil.ints.IntArrayList
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.utility.get

class NPCTrackingSet : Iterable<NPC> {

    val locals = IntArrayList(LOCAL_NPC_CAP)

    fun clear() {
        locals.clear()
    }

    override fun iterator(): Iterator<NPC> {
        val npcs: NPCs = get()
        return object : Iterator<NPC> {
            var index = 0
            override fun hasNext(): Boolean {
                return index < locals.size
            }

            override fun next(): NPC {
                return npcs.indexed(locals.getInt(index++))!!
            }
        }
    }

    companion object {
        const val LOCAL_NPC_CAP = 255
    }
}

val NPC.teleporting: Boolean
    get() = movement.delta != Delta.EMPTY && movement.walkStep == Direction.NONE && movement.runStep == Direction.NONE
