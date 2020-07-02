package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 2, 2020
 */
data class ObjectPreloadMessage(
    val id: Int,
    val type: Int
) : Message