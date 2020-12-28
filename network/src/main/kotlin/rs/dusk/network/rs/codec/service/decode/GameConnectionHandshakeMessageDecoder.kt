package rs.dusk.network.rs.codec.service.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.service.ServiceMessageDecoder
import rs.dusk.network.rs.codec.service.decode.message.GameConnectionHandshakeMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class GameConnectionHandshakeMessageDecoder : ServiceMessageDecoder<GameConnectionHandshakeMessage>(0) {
    override fun decode(packet: PacketReader): GameConnectionHandshakeMessage {
        return GameConnectionHandshakeMessage()
    }

}