package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.entity.Despawn
import world.gregs.voidps.engine.entity.MAX_PLAYERS
import world.gregs.voidps.engine.entity.character.CharacterSearch
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

class Players :
    Iterable<Player>,
    CharacterSearch<Player> {
    private val players = mutableListOf<Player>()
    private val indexArray: Array<Player?> = arrayOfNulls(MAX_PLAYERS)
    private var indexer = 1
    val size: Int
        get() = players.size

    fun get(name: String): Player? = firstOrNull { it.name == name }

    fun add(player: Player): Boolean {
        if (player.index == -1 || indexArray[player.index] != null) {
            return false
        }
        indexArray[player.index] = player
        return players.add(player)
    }

    fun remove(player: Player): Boolean {
        indexArray[player.index] = null
        return players.remove(player)
    }

    internal fun index(): Int? {
        if (indexer < indexArray.size) {
            return indexer++
        }
        for (i in 1 until indexArray.size) {
            if (indexArray[i] == null) {
                return i
            }
        }
        return null
    }

    fun indexed(index: Int): Player? = indexArray[index]

    override operator fun get(tile: Tile) = players.filter { it.tile == tile }

    override operator fun get(zone: Zone) = players.filter { it.tile.zone == zone }

    fun clear() {
        for (player in this) {
            player.emit(Despawn)
        }
        indexArray.fill(null)
        players.clear()
        indexer = 1
    }

    fun shuffle() = players.shuffle()

    override fun iterator(): Iterator<Player> = players.iterator()
}
