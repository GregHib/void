package world.gregs.voidps.engine.entity.character.npc

import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating.Companion.VIEW_RADIUS
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.CharacterTrackingSet
import world.gregs.voidps.engine.entity.character.ViewState
import world.gregs.voidps.engine.entity.list.MAX_NPCS
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.utility.get

class NPCTrackingSet(
    val tickAddMax: Int,
    override val localMax: Int,
    override val radius: Int = VIEW_RADIUS - 1
) : CharacterTrackingSet<NPC>, Iterable<NPC> {

    override val locals = IntArray(localMax)
    override val state = ViewState(MAX_NPCS)
    override var lastIndex = 0
    override var addCount = 0
    override var total: Int = 0

    val add = IntArray(tickAddMax)
    val addIndices: IntRange
        get() = 0 until addCount

    fun remove(index: Int): Boolean = state.removing(index)

    fun refresh() {
        for (i in indices) {
            val index = locals[i]
            if (addCount < tickAddMax) {
                add[addCount++] = index
                state.setAdding(index)
            }
        }
        lastIndex = 0
        total = 0
    }

    override fun track(entity: NPC, self: NPC?) {
        if (state.removing(entity.index) && !entity.teleporting) {
            state.setLocal(entity.index)
            total++
        } else if (addCount < tickAddMax) {
            add[addCount++] = entity.index
            state.setAdding(entity.index)
            total++
        }
    }

    override fun iterator(): Iterator<NPC> {
        val npcs: NPCs = get()
        return object : Iterator<NPC> {
            var index = 0
            override fun hasNext(): Boolean {
                return index < lastIndex
            }

            override fun next(): NPC {
                return npcs.indexed(locals[index++])!!
            }
        }
    }
}

val NPC.teleporting: Boolean
    get() = movement.delta != Delta.EMPTY && movement.walkStep == Direction.NONE && movement.runStep == Direction.NONE
