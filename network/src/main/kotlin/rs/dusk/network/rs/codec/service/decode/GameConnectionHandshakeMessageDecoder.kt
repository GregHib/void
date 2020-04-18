package rs.dusk.network.rs.codec.service.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.service.ServiceMessageDecoder
import rs.dusk.network.rs.codec.service.ServiceOpcodes.GAME_CONNECTION
import rs.dusk.network.rs.codec.service.decode.message.GameConnectionHandshakeMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
@PacketMetaData(opcodes = [GAME_CONNECTION], length = 0)
class GameConnectionHandshakeMessageDecoder : ServiceMessageDecoder<GameConnectionHandshakeMessage>() {
    override fun decode(packet: PacketReader): GameConnectionHandshakeMessage {
        return GameConnectionHandshakeMessage()
    }

}