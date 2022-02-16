package world.gregs.voidps.engine.entity.character.npc

import it.unimi.dsi.fastutil.ints.IntOpenHashSet
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
    val add = IntArray(tickAddMax)
    var addLastIndex = 0
    val remove: MutableSet<Int> = IntOpenHashSet()
    val current = IntArray(localMax)
    var localLastIndex = 0

    fun remove(index: Int): Boolean = remove.contains(index)

    override fun start(self: NPC?) {
        for (i in 0 until localLastIndex) {
            remove.add(current[i])
        }
        total = 0
    }

    override fun update() {
        val add = (0 until addLastIndex).map { add[it] }.toSet()
        val cur = (0 until localLastIndex).map { current[it] }.toSet()
        localLastIndex = 0
        for (index in 0 until MAX_NPCS) {
            if (add.contains(index)) {
                current[localLastIndex++] = index
            } else if(cur.contains(index) && !remove.contains(index)) {
                current[localLastIndex++] = index
            }
        }
        addLastIndex = 0
        remove.clear()
        total = localLastIndex//current.size
    }

    fun refresh() {
        for (i in 0 until localLastIndex) {
            val index = current[i]
            if (addLastIndex < tickAddMax) {
                add[addLastIndex++] = index
            }
        }
        localLastIndex = 0
        total = 0
    }

    override fun track(entity: NPC, self: NPC?) {
        val visible = !entity.teleporting && remove.removeIf { it == entity.index }
        if (visible) {
            total++
        } else if (addLastIndex < tickAddMax) {
            add[addLastIndex++] = entity.index
            total++
        }
    }

    override fun iterator(): Iterator<NPC> {
        val npcs: NPCs = get()
        return current.map { npcs.indexed(it)!! }.iterator()
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
