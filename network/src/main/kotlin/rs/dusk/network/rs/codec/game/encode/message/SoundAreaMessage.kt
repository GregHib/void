package rs.dusk.network.rs.codec.game.encode.message

import rs.dusk.core.network.model.message.Message

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 2, 2020
 */
data class SoundAreaMessage(
    val tile: Int,
    val id: Int,
    val type: Int,
    val rotation: Int,
    val three: Int,
    val four: Int,
    val five: Int
) : Message