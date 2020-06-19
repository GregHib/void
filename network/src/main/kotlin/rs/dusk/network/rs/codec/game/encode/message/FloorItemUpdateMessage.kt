package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 19, 2020
 * @param offset The tile offset from the [ChunkMessage] sent
 * @param id Item id
 * @param oldAmount Previous item stack size
 * @param newAmount Updated item stack size
 */
data class FloorItemUpdateMessage(
    val offset: Int,
    val id: Int,
    val oldAmount: Int,
    val newAmount: Int
) : Message