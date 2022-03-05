package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.entity.character.player.Viewport.Companion.VIEW_RADIUS
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
    val localMax: Int,
    val radius: Int = VIEW_RADIUS - 1
) : Iterable<Player> {

    val appearanceHash = IntArray(MAX_PLAYERS)
    val localPlayers = BooleanArray(MAX_PLAYERS)
    val localPlayersIndexes = IntArray(MAX_PLAYERS)
    var localPlayersIndexesCount = 0
    val totalRenderDataSentLength = 0

    val outPlayersIndexes = IntArray(MAX_PLAYERS)
    var outPlayersIndexesCount = 0

    fun addSelf(self: Player) {
        localPlayers[self.index] = true
        localPlayersIndexes[localPlayersIndexesCount++] = self.index
        for (i in 1 until MAX_PLAYERS) {
            if(i == self.index) {
                continue
            }
            outPlayersIndexes[outPlayersIndexesCount++] = i
        }
    }

    fun update() {
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
    }

    override fun iterator(): Iterator<Player> {
        val players: Players = get()
        return object : Iterator<Player> {
            var index = 0
            override fun hasNext(): Boolean {
                return index < localPlayersIndexesCount
            }

            override fun next(): Player {
                return players.indexed(localPlayersIndexes[index++])!!
            }
        }
    }
}
