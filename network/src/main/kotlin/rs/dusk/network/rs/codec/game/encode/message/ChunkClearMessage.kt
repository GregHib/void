package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 19, 2020
 * @param xOffset The chunk x coordinate relative to viewport
 * @param yOffset The chunk y coordinate relative to viewport
 * @param plane The chunks plane
 */
data class ChunkClearMessage(
    val xOffset: Int,
    val yOffset: Int,
    val plane: Int
) : Message