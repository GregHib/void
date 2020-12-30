package rs.dusk.engine.entity.character.player

import rs.dusk.buffer.write.BufferWriter
import rs.dusk.engine.client.update.task.viewport.ViewportUpdating.Companion.LOCAL_NPC_CAP
import rs.dusk.engine.client.update.task.viewport.ViewportUpdating.Companion.LOCAL_PLAYER_CAP
import rs.dusk.engine.client.update.task.viewport.ViewportUpdating.Companion.NPC_TICK_CAP
import rs.dusk.engine.client.update.task.viewport.ViewportUpdating.Companion.PLAYER_TICK_CAP
import rs.dusk.engine.entity.character.npc.NPCTrackingSet
import rs.dusk.engine.entity.list.MAX_PLAYERS
import rs.dusk.engine.map.chunk.Chunk

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 21, 2020
 */
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

    val playerChanges = BufferWriter()
    val playerUpdates = BufferWriter()
    val npcChanges = BufferWriter()
    val npcUpdates = BufferWriter()

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
        val VIEWPORT_SIZES = intArrayOf(104, 120, 136, 168)
    }
}