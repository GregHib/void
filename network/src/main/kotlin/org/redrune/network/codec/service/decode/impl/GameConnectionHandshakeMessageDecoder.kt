package org.redrune.network.codec.service.decode.impl

import org.redrune.core.network.packet.PacketMetaData
import org.redrune.core.network.packet.access.PacketReader
import org.redrune.network.codec.service.decode.ServiceMessageDecoder
import org.redrune.network.codec.service.decode.message.GameConnectionHandshakeMessage
import org.redrune.tools.constants.ServiceOpcodes.GAME_CONNECTION

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