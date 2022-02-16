package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating.Companion.LOCAL_PLAYER_CAP
import world.gregs.voidps.engine.client.update.task.viewport.ViewportUpdating.Companion.VIEW_RADIUS
import world.gregs.voidps.engine.entity.character.CharacterTrackingSet
import world.gregs.voidps.engine.entity.list.MAX_PLAYERS
import world.gregs.voidps.engine.utility.get

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
        for (i in 0 until lastIndex) {
            val index = locals[i]
            if (index != self?.index) {
                state[index] = REMOVING
            }
        }
        total = if (self != null) 1 else 0
    }

    override fun update() {
        addCount = 0
        lastIndex = 0
        for (i in 1 until MAX_PLAYERS) {
            if (state[i] == REMOVING) {
                state[i] = GLOBAL
            } else if (state[i] == ADDING) {
                state[i] = LOCAL
                locals[lastIndex++] = i
            } else if (state[i] == LOCAL) {
                locals[lastIndex++] = i
            }
        }
        total = lastIndex
    }

    override fun addSelf(self: Player) {
        if (state[self.index] != LOCAL) {
            locals[lastIndex++] = self.index
        }
        state[self.index] = LOCAL
        total++
    }

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
