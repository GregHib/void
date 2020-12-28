package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.decode.message.PingMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
class PingMessageDecoder : GameMessageDecoder<PingMessage>(0) {

    override fun decode(packet: PacketReader): PingMessage {
        return PingMessage
    }

}