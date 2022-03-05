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


    val appearanceHash = IntArray(MAX_PLAYERS)
    val localPlayers = BooleanArray(MAX_PLAYERS)
    val localPlayersIndexes = IntArray(MAX_PLAYERS)
    var localPlayersIndexesCount = 0
    val totalRenderDataSentLength = 0

    val outPlayersIndexes = IntArray(MAX_PLAYERS)
    var outPlayersIndexesCount = 0
    val globals = IntArray(MAX_PLAYERS)
    var globalIndex = 0
    val locals = IntArray(LOCAL_PLAYER_CAP)
    override val state = ViewState(MAX_PLAYERS)
    var lastIndex = 0
    var addCount = 0
    override var total: Int = 0
    val indices: IntRange
        get() = 0 until lastIndex

    override fun start(self: Player?) {
//        var index: Int
//        for (i in indices) {
//            index = locals[i]
//            if (index != self?.index) {
//                state.setRemoving(index)
//            }
//        }
//        total = if (self != null) 1 else 0
    }

    fun addSelf(self: Player) {
        localPlayers[self.index] = true
        localPlayersIndexes[localPlayersIndexesCount++] = self.index
//        if (!state.local(self.index)) {
//            locals[lastIndex++] = self.index
//            state.setLocal(self.index)
        for (i in 1 until MAX_PLAYERS) {
            if(i == self.index) {
                continue
            }
            outPlayersIndexes[outPlayersIndexesCount++] = i
        }
//        }
        total++
    }

    override fun update(characters: CharacterList<Player>) {
        lastIndex = 0
        globalIndex = 0

        localPlayersIndexesCount = 0
        outPlayersIndexesCount = 0
        for (i in 1 until MAX_PLAYERS) {
            val local = localPlayers[i]
            if (!local) {
                outPlayersIndexes[outPlayersIndexesCount++] = i
            } else {
                localPlayersIndexes[localPlayersIndexesCount++] = i
            }
        }
//        for (index in 1 until MAX_PLAYERS/*characters.indexer.cap*/) {
//            if (locals[index] == 0) {// FIXME
//                state.setGlobal(index)
//                globals[globalIndex++] = index
//            } else {
//                state.setLocal(index)
//                locals[lastIndex++] = index
//            }
//            /*if (state.removing(index)) {
//                state.setGlobal(index)
//            } else if (state.adding(index) || state.local(index)) {
//                state.setLocal(index)
//                locals[lastIndex++] = index
//            }*/
//        }
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
