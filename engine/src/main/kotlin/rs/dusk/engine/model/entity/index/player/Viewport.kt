package rs.dusk.engine.model.entity.index.player

import rs.dusk.engine.client.update.ViewportUpdating.Companion.LOCAL_NPC_CAP
import rs.dusk.engine.client.update.ViewportUpdating.Companion.LOCAL_PLAYER_CAP
import rs.dusk.engine.client.update.ViewportUpdating.Companion.NPC_TICK_CAP
import rs.dusk.engine.client.update.ViewportUpdating.Companion.PLAYER_TICK_CAP
import rs.dusk.engine.model.entity.index.npc.NPCTrackingSet
import rs.dusk.engine.model.entity.list.MAX_PLAYERS
import rs.dusk.engine.model.world.Chunk
import rs.dusk.network.rs.codec.game.encode.message.NPCUpdateMessage
import rs.dusk.network.rs.codec.game.encode.message.PlayerUpdateMessage

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
    var size: Int = VIEWPORT_SIZES[0],
    val regions: MutableSet<Int> = linkedSetOf(),
    var lastLoadChunk: Chunk = Chunk.EMPTY,
    var loaded: Boolean = false
) {

    val message = PlayerUpdateMessage()
    val npcMessage = NPCUpdateMessage()

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