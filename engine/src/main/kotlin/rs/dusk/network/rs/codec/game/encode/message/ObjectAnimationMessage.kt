package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 27, 2020
 * @param tileId 30 bit location hash
 * @param animation Animation id
 * @param type Object type
 * @param rotation Object rotation
 */
data class ObjectAnimationMessage(
    val tileId: Int,
    var animation: Int,
    var type: Int,
    var rotation: Int
) : Message