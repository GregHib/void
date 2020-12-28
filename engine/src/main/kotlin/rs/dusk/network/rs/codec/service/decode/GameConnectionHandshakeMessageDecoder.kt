package rs.dusk.network.rs.codec.service.decode

import rs.dusk.core.network.codec.message.MessageDecoder
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.service.decode.message.GameConnectionHandshakeMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class GameConnectionHandshakeMessageDecoder : MessageDecoder<GameConnectionHandshakeMessage>(0) {
    override fun decode(packet: PacketReader): GameConnectionHandshakeMessage {
        return GameConnectionHandshakeMessage()
    }

}