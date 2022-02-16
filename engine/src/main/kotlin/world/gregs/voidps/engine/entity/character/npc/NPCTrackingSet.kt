package world.gregs.voidps.engine.entity.character.npc

import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating.Companion.VIEW_RADIUS
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.CharacterTrackingSet
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
    val current: LinkedHashSet<Int> = LinkedHashSet()

    fun remove(index: Int): Boolean = remove.contains(index)

    override fun start(self: NPC?) {
        remove.addAll(current)
        total = 0
    }

    override fun update() {
        remove.forEach {
            current.remove(it)
        }
        for (i in 0 until addLastIndex) {
            current.add(add[i])
        }
        addLastIndex = 0
        remove.clear()
        total = current.size
    }

    fun refresh() {
        for (index in current) {
            if (addLastIndex < tickAddMax) {
                add[addLastIndex++] = index
            }
        }
        current.clear()
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
