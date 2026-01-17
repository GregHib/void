package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.entity.Despawn
import world.gregs.voidps.engine.entity.MAX_PLAYERS
import world.gregs.voidps.engine.entity.character.CharacterSearch
import world.gregs.voidps.engine.entity.character.CharacterIndexMap
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

class Players :
    Iterable<Player>,
    CharacterSearch<Player> {
    private val players = mutableListOf<Player>()
    private val indexArray: Array<Player?> = arrayOfNulls(MAX_PLAYERS)
    private var indexer = 1
    private val map = CharacterIndexMap(MAX_PLAYERS)
    val size: Int
        get() = players.size

    fun get(name: String): Player? = firstOrNull { it.name == name }

    fun add(player: Player): Boolean {
        if (player.index == -1 || indexArray[player.index] != null) {
            return false
        }
        map.add(player.tile.zone.id, player.index)
        indexArray[player.index] = player
        return players.add(player)
    }

    fun update(player: Player, from: Tile) {
        if (player.tile.zone != from.zone) {
            map.remove(from.zone.id, player.index)
            map.add(player.tile.zone.id, player.index)
        }
    }

    fun remove(player: Player): Boolean {
        indexArray[player.index] = null
        map.remove(player.tile.zone.id, player.index)
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

    override fun at(tile: Tile): List<Player> {
        val list = mutableListOf<Player>()
        map.onEach(tile.zone.id) { index ->
            val player = indexed(index) ?: return@onEach
            if (player.tile == tile) {
                list.add(player)
            }
        }
        return list
    }

    override operator fun get(zone: Zone): List<Player> {
        val list = mutableListOf<Player>()
        map.onEach(zone.id) { index ->
            list.add(indexed(index) ?: return@onEach)
        }
        return list
    }

    fun clear() {
        for (player in this) {
            Despawn.player(player)
        }
        indexArray.fill(null)
        players.clear()
        indexer = 1
        map.clear()
    }

    fun shuffle() = players.shuffle()

    override fun iterator(): Iterator<Player> = players.iterator()
}
