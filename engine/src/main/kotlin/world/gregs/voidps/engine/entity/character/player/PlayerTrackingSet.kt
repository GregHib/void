package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating.Companion.LOCAL_PLAYER_CAP
import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating.Companion.VIEW_RADIUS
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.CharacterTrackingSet
import world.gregs.voidps.engine.entity.list.MAX_PLAYERS
import world.gregs.voidps.engine.utility.get

/**
 * Keeps track of players moving in and out of view
 * Each tick [start] clears the view of all players except self then
 * [ViewportUpdating] re-adds all players still within view and queues new
 * additions to be added the following tick.
 */
class PlayerTrackingSet(
    val tickAddMax: Int,
    override val localMax: Int,
    override val radius: Int = VIEW_RADIUS - 1
) : CharacterTrackingSet<Player>, Iterable<Player> {

    val locals = IntArray(LOCAL_PLAYER_CAP)
    val state = IntArray(MAX_PLAYERS)
    val indices: IntRange
        get() = 0 until lastIndex
    var lastIndex = 0
    var addCount = 0
        private set
    override var total: Int = 0

    fun remove(index: Int) = state[index] == REMOVING

    fun local(index: Int) = state[index] == LOCAL || state[index] == REMOVING

    fun add(index: Int) = state[index] == ADDING

    override fun start(self: Player?) {
        for (i in indices) {
            val index = locals[i]
            if (index != self?.index) {
                state[index] = REMOVING
            }
        }
        total = if (self != null) 1 else 0
    }

    override fun update(characters: CharacterList<Player>) {
        addCount = 0
        lastIndex = 0
        for (index in 1 until characters.indexer.cap) {
            when (state[index]) {
                REMOVING -> state[index] = GLOBAL
                ADDING, LOCAL -> {
                    state[index] = LOCAL
                    locals[lastIndex++] = index
                }
            }
        }
        total = lastIndex
    }

    fun addSelf(self: Player) {
        if (state[self.index] != LOCAL) {
            locals[lastIndex++] = self.index
        }
        state[self.index] = LOCAL
        total++
    }

    /**
     * If an entity is being removed, return it to the local list
     * Otherwise queue it if there is room on the addition list
     */
    override fun track(entity: Player, self: Player?) {
        if (state[entity.index] == REMOVING) {
            state[entity.index] = LOCAL
            total++
        } else if (self == null || entity != self) {
            if (addCount < tickAddMax) {
                state[entity.index] = ADDING
                addCount++
                total++
            }
        }
    }

    override fun iterator(): Iterator<Player> {
        val players: Players = get()
        return object : Iterator<Player> {
            var index = 0
            override fun hasNext(): Boolean {
                return index < lastIndex
            }

            override fun next(): Player {
                return players.indexed(locals[index++])!!
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
