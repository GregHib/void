package world.gregs.voidps.engine.entity.character.player

import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue
import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating.Companion.PLAYER_TICK_CAP
import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating.Companion.VIEW_RADIUS
import world.gregs.voidps.engine.entity.character.CharacterTrackingSet
import world.gregs.voidps.engine.entity.list.MAX_PLAYERS
import java.util.*

class PlayerTrackingSet(
    val tickMax: Int,
    override val maximum: Int,
    override val radius: Int = VIEW_RADIUS - 1,
    val add: ObjectArrayFIFOQueue<Player> = ObjectArrayFIFOQueue(PLAYER_TICK_CAP),
    val remove: MutableSet<Player> = mutableSetOf(),
    override val current: MutableSet<Player> = TreeSet()// Ordered locals
) : CharacterTrackingSet<Player> {

    val state = IntArray(MAX_PLAYERS)

    fun remove(index: Int) = state[index] == REMOVING

    fun local(index: Int) = state[index] == LOCAL || state[index] == REMOVING

    fun add(index: Int) = state[index] == ADDING

    override var total: Int = 0

    override fun start(self: Player?) {
        for (p in current) {
            state[p.index] = REMOVING
        }
        remove.addAll(current)
        total = 0
        if (self != null) {
            addSelf(self)
        }
    }

    override fun finish() {
    }

    override fun update() {
        remove.forEach {
            if (state[it.index] == REMOVING) {
                state[it.index] = GLOBAL
                current.remove(it)
            }
        }
        while (!add.isEmpty) {
            val it = add.dequeue()
            if (state[it.index] == ADDING) {
                state[it.index] = LOCAL
                current.add(it)
            }
        }
        remove.clear()
        total = current.size
    }

    override fun addSelf(self: Player) {
        current.add(self)
        state[self.index] = LOCAL
        total++
    }

    override fun track(entity: Player, self: Player?) {
        if (state[entity.index] == REMOVING) {
            state[entity.index] = LOCAL
            remove.remove(entity)
            total++
        } else if (self == null || entity != self) {
            if (add.size() < tickMax) {
                state[entity.index] = ADDING
                add.enqueue(entity)
                total++
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
