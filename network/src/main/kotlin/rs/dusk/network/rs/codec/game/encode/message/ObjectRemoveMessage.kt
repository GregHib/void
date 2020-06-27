package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 27, 2020
 * @param offset The tile offset from the [ChunkUpdateMessage] sent
 * @param type Object type
 * @param rotation Object rotation
 */
data class ObjectRemoveMessage(
    val offset: Int,
    var type: Int,
    var rotation: Int
) : Message