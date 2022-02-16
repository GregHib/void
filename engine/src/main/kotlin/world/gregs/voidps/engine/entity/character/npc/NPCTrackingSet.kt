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
    override val radius: Int = VIEW_RADIUS - 1,
    val add: LinkedHashSet<Int> = LinkedHashSet(),
    val remove: MutableSet<Int> = IntOpenHashSet(),
    val current: LinkedHashSet<NPC> = LinkedHashSet()
) : CharacterTrackingSet<NPC>, Iterable<NPC> {

    override fun iterator(): Iterator<NPC> {
        return current.iterator()
    }

    override var total: Int = 0

    fun remove(character: NPC): Boolean {
        return remove.contains(character.index)
    }

    override fun start(self: NPC?) {
        remove.addAll(current.map { it.index })
        total = 0
    }

    override fun update() {
        remove.forEach {
            current.removeIf { npc -> npc.index == it }
        }
        val npcs: NPCs = get()
        add.forEach { index ->
            current.add(npcs.indexed(index)!!)
        }
        remove.clear()
        add.clear()
        total = current.size
    }

    fun refresh() {
        add.addAll(current.map { it.index })
        current.clear()
        total = 0
    }

    override fun track(entity: NPC, self: NPC?) {
        val visible = !entity.teleporting && remove.removeIf { it == entity.index }
        if (visible) {
            total++
        } else if (add.size < tickAddMax) {
            add.add(entity.index)
            total++
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
