package world.gregs.voidps.engine.entity.character.npc

import it.unimi.dsi.fastutil.ints.IntArrayList
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.player.Viewport.Companion.VIEW_RADIUS
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.utility.get

class NPCTrackingSet(
    val tickAddMax: Int,
    val localMax: Int,
    val radius: Int = VIEW_RADIUS - 1
) : Iterable<NPC> {

    val locals = IntArrayList(localMax)

    fun refresh() {
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
}

val NPC.teleporting: Boolean
    get() = movement.delta != Delta.EMPTY && movement.walkStep == Direction.NONE && movement.runStep == Direction.NONE
