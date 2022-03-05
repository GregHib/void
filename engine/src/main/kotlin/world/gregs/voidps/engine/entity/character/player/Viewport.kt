package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.engine.entity.character.npc.NPCTrackingSet
import world.gregs.voidps.engine.entity.list.MAX_PLAYERS
import world.gregs.voidps.engine.map.chunk.Chunk

@Suppress("ArrayInDataClass")
data class Viewport(
    val players: PlayerTrackingSet = PlayerTrackingSet(
        PLAYER_TICK_CAP,
        LOCAL_PLAYER_CAP
    ),
    val npcs: NPCTrackingSet = NPCTrackingSet(
        NPC_TICK_CAP,
        LOCAL_NPC_CAP
    ),
    val idlePlayers: IntArray = IntArray(MAX_PLAYERS),
    var lastLoadChunk: Chunk = Chunk.EMPTY,
    var loaded: Boolean = false,
    var dynamic: Boolean = false
) {

    var size: Int = 0
    val tileSize: Int
        get() = VIEWPORT_SIZES[size]

    val playerChanges = BufferWriter(3000)
    val playerUpdates = BufferWriter(7500)
    val npcChanges = BufferWriter(4096)
    val npcUpdates = BufferWriter(4096)

    val lastSeen = IntArray(MAX_PLAYERS)

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
        const val PLAYER_TICK_CAP = 15
        const val NPC_TICK_CAP = 40
        const val LOCAL_PLAYER_CAP = 255
        const val LOCAL_NPC_CAP = 255

        // View radius could be controlled per tracking set to give a nicer linear
        // expanding square when loading areas with more than max entities
        const val VIEW_RADIUS = 15
        val VIEWPORT_SIZES = intArrayOf(104, 120, 136, 168)
    }
}