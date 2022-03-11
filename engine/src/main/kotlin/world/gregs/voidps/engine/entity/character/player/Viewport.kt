package world.gregs.voidps.engine.entity.character.player

import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.engine.entity.list.MAX_PLAYERS
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.chunk.Chunk

class Viewport {

    val players: PlayerTrackingSet = PlayerTrackingSet()
    val npcs = IntLinkedOpenHashSet(LOCAL_NPC_CAP)
    val idlePlayers: IntArray = IntArray(MAX_PLAYERS)
    private val lastSeen = IntArray(MAX_PLAYERS)

    val playerChanges = BufferWriter(3000)
    val playerUpdates = BufferWriter(7500)
    val npcChanges = BufferWriter(3000)
    val npcUpdates = BufferWriter(4000)

    var lastLoadChunk: Chunk = Chunk.EMPTY
    var loaded: Boolean = false
    var dynamic: Boolean = false
    var size: Int = 0
    val tileSize: Int
        get() = VIEWPORT_SIZES[size]
    val chunkRadius: Int
        get() = tileSize shr 4
    val localRadius: Int
        get() = tileSize shr 5
    val chunkArea: Int
        get() = tileSize / 8

    var radius: Int = VIEW_RADIUS

    fun seen(player: Player) {
        lastSeen[player.index] = player.tile.id
    }

    fun lastSeen(player: Player): Tile {
        return Tile(lastSeen[player.index])
    }

    fun isActive(index: Int) = idlePlayers[index] and 0x1 == 0

    fun isIdle(index: Int) = idlePlayers[index] and 0x1 != 0

    fun setIdle(index: Int) {
        idlePlayers[index] = idlePlayers[index] or 2
    }

    fun shift() {
        for (index in idlePlayers.indices) {
            idlePlayers[index] = idlePlayers[index] shr 1
        }
    }

    companion object {
        const val LOCAL_NPC_CAP = 250
        // View radius could be controlled per tracking set to give a nicer linear
        // expanding square when loading areas with more than max entities
        const val VIEW_RADIUS = 15
        val VIEWPORT_SIZES = intArrayOf(104, 120, 136, 168)
    }
}