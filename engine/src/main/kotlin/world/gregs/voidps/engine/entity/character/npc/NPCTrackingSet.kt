package world.gregs.voidps.engine.entity.character.npc

import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating.Companion.VIEW_RADIUS
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.CharacterTrackingSet
import world.gregs.voidps.engine.entity.list.MAX_NPCS
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.utility.get

class NPCTrackingSet(
    val tickAddMax: Int,
    override val localMax: Int,
    override val radius: Int = VIEW_RADIUS - 1
) : CharacterTrackingSet<NPC>, Iterable<NPC> {

    override var total: Int = 0
    val locals = IntArray(localMax)
    val add = IntArray(tickAddMax)
    var localLastIndex = 0
    var addLastIndex = 0
    val addIndices: IntRange
        get() = 0 until addLastIndex
    val localIndices: IntRange
        get() = 0 until localLastIndex

    val state = IntArray(MAX_NPCS)

    fun remove(index: Int): Boolean = state[index] == REMOVING

    override fun start(self: NPC?) {
        for (i in localIndices) {
            val index = locals[i]
            state[index] = REMOVING
        }
        total = 0
    }

    override fun update() {
        localLastIndex = 0
        for (index in 0 until MAX_NPCS) {
            when (state[index]) {
                REMOVING -> state[index] = GLOBAL
                ADDING, LOCAL -> {
                    state[index] = LOCAL
                    locals[localLastIndex++] = index
                }
            }
        }
        addLastIndex = 0
        total = localLastIndex
    }

    fun refresh() {
        for (i in localIndices) {
            val index = locals[i]
            if (addLastIndex < tickAddMax) {
                add[addLastIndex++] = index
                state[index] = ADDING
            }
        }
        localLastIndex = 0
        total = 0
    }

    override fun track(entity: NPC, self: NPC?) {
        if (state[entity.index] == REMOVING && !entity.teleporting) {
            state[entity.index] = LOCAL
            total++
        } else if (addLastIndex < tickAddMax) {
            add[addLastIndex++] = entity.index
            state[entity.index] = ADDING
            total++
        }
    }

    override fun iterator(): Iterator<NPC> {
        val npcs: NPCs = get()
        return object : Iterator<NPC> {
            var index = 0
            override fun hasNext(): Boolean {
                return index < localLastIndex
            }

            override fun next(): NPC {
                return npcs.indexed(locals[index++])!!
            }

        }
    }

    companion object {
        private const val GLOBAL = 0
        private const val LOCAL = 1
        private const val ADDING = 2
        private const val REMOVING = 3
    }
}

val NPC.teleporting: Boolean
    get() = movement.delta != Delta.EMPTY && movement.walkStep == Direction.NONE && movement.runStep == Direction.NONE
