package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 2, 2020
 */
data class TextTileMessage(
    val tile: Int,
    val duration: Int,
    val height: Int,
    val color: Int,
    val text: String
) : Message