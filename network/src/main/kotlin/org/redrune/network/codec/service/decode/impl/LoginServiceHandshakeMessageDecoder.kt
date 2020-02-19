package org.redrune.network.codec.service.decode.impl

import org.redrune.network.codec.service.decode.ServiceMessageDecoder
import org.redrune.network.codec.service.decode.message.LoginServiceHandshakeMessage
import org.redrune.network.packet.PacketMetaData
import org.redrune.network.packet.PacketType
import org.redrune.network.packet.PacketType.Companion.VARIABLE_LENGTH_SHORT
import org.redrune.network.packet.PacketType.SHORT
import org.redrune.network.packet.access.PacketReader
import org.redrune.tools.constants.ServiceOpcodes.GAME_CONNECTION

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
@PacketMetaData(opcodes = [GAME_CONNECTION], length = 0)
class LoginServiceHandshakeMessageDecoder : ServiceMessageDecoder<LoginServiceHandshakeMessage>() {
    override fun decode(packet: PacketReader): LoginServiceHandshakeMessage {
        return LoginServiceHandshakeMessage()
    }

}