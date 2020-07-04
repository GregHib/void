package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 27, 2020
 * @param offset The tile offset from the [ChunkUpdateMessage] sent
 * @param animation Animation id
 * @param type Object type
 * @param rotation Object rotation
 */
data class ObjectAnimationSpecificMessage(
    val offset: Int,
    val animation: Int,
    val type: Int,
    val rotation: Int
) : Message