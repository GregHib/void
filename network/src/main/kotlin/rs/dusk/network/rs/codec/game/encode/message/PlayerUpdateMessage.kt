package rs.dusk.network.rs.codec.game.encode.message

import io.netty.buffer.ByteBuf
import rs.dusk.core.network.model.message.Message

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
data class PlayerUpdateMessage(val changes: ByteBuf, val updates: ByteBuf) : Message