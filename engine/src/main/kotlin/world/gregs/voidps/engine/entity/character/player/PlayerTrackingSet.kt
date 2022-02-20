package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating.Companion.LOCAL_PLAYER_CAP
import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating.Companion.VIEW_RADIUS
import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.CharacterTrackingSet
import world.gregs.voidps.engine.entity.character.ViewState
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
    override val state = ViewState(MAX_PLAYERS)
    var lastIndex = 0
    var addCount = 0
    override var total: Int = 0
    val indices: IntRange
        get() = 0 until lastIndex

    override fun start(self: Player?) {
        var index: Int
        for (i in indices) {
            index = locals[i]
            if (index != self?.index) {
                state.setRemoving(index)
            }
        }
        total = if (self != null) 1 else 0
    }

    fun addSelf(self: Player) {
        if (!state.local(self.index)) {
            locals[lastIndex++] = self.index
            state.setLocal(self.index)
        }
        total++
    }

    override fun update(characters: CharacterList<Player>) {
        lastIndex = 0
        for (index in 1 until characters.indexer.cap) {
            if (state.removing(index)) {
                state.setGlobal(index)
            } else if (state.adding(index) || state.local(index)) {
                state.setLocal(index)
                locals[lastIndex++] = index
            }
        }
        addCount = 0
        total = lastIndex
    }

    override fun track(entity: Int, self: Boolean) {
        if (state.removing(entity)) {
            state.setLocal(entity)
            total++
        } else if (!self) {
            if (addCount < tickAddMax) {
                state.setAdding(entity)
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
}
