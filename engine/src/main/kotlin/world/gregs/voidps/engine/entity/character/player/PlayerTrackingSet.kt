package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating.Companion.VIEW_RADIUS
import world.gregs.voidps.engine.entity.character.CharacterTrackingSet
import world.gregs.voidps.engine.entity.list.MAX_PLAYERS
import world.gregs.voidps.engine.map.Tile
import java.util.*

class PlayerTrackingSet(
    val tickMax: Int,
    override val maximum: Int,
    override val radius: Int = VIEW_RADIUS - 1,
    val add: LinkedHashSet<Player> = LinkedHashSet(),
    val remove: MutableSet<Player> = mutableSetOf(),
    override val current: MutableSet<Player> = TreeSet(),// Ordered locals
    val lastSeen: MutableMap<Player, Tile> = mutableMapOf()
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
        remove.forEach {
            lastSeen[it] = it.movement.trailingTile
        }
    }

    override fun update() {
        remove.forEach {
            if (state[it.index] == REMOVING) {
                state[it.index] = GLOBAL
                current.remove(it)
                lastSeen[it] = it.tile
            }
        }
        add.forEach {
            if (state[it.index] == ADDING) {
                state[it.index] = LOCAL
                current.add(it)
                lastSeen[it] = it.tile
            }
        }
        remove.clear()
        add.clear()
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
            if (add.size < tickMax) {
                state[entity.index] = ADDING
                add.add(entity)
                total++
            }
        }
    }

    companion object {
        private const val PLAYER_TICK_CAP = 40
        const val LOCAL_PLAYER_CAP = 255

        private const val GLOBAL = 0
        private const val LOCAL = 1
        private const val ADDING = 2
        private const val REMOVING = 3
    }
}
