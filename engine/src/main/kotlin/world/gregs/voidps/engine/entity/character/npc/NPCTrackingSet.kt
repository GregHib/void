package world.gregs.voidps.engine.entity.character.npc

import it.unimi.dsi.fastutil.ints.IntArrayList
import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating.Companion.VIEW_RADIUS
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.CharacterList
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

    val locals = IntArrayList(localMax)
    override val state = ViewState(MAX_NPCS)
    override var total: Int = 0

    val add = IntArray(tickAddMax)
    var addCount = 0
    val addIndices: IntRange
        get() = 0 until addCount

    override fun start(self: NPC?) {
        for (index in locals.intIterator()) {
            state.setRemoving(index)
        }
        total = 0
    }

    fun refresh() {
        var index: Int
        while (locals.isNotEmpty()) {
            index = locals.popInt()
            if (addCount < tickAddMax) {
                add[addCount++] = index
                state.setAdding(index)
            }
        }
        total = 0
    }

    @Suppress("DEPRECATION")
    override fun update(characters: CharacterList<NPC>) {
        for (index in 1 until characters.indexer.cap) {
            if (state.removing(index)) {
                locals.remove(index)
                state.setGlobal(index)
            }
        }
        var index: Int
        for (i in 0 until addCount) {
            index = add[i]
            if (state.adding(index)) {
                locals.add(index)
                state.setLocal(index)
            }
        }
        addCount = 0
        total = locals.size
    }

    override fun track(entity: Int, self: Boolean) {
        if (state.removing(entity) /* && !entity.teleporting*/) {// FIXME
            state.setLocal(entity)
            total++
        } else if (state.global(entity) && addCount < tickAddMax) {
            add[addCount++] = entity
            state.setAdding(entity)
            total++
        }
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
