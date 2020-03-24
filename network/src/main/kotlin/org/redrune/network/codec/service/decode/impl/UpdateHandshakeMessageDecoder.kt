package org.redrune.network.codec.service.decode.impl

import org.redrune.core.network.packet.PacketMetaData
import org.redrune.core.network.packet.access.PacketReader
import org.redrune.network.codec.service.decode.ServiceMessageDecoder
import org.redrune.network.codec.service.decode.message.UpdateHandshakeMessage
import org.redrune.tools.constants.ServiceOpcodes.FILE_SERVICE

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
@PacketMetaData(opcodes = [FILE_SERVICE], length=4)
class UpdateHandshakeMessageDecoder : ServiceMessageDecoder<UpdateHandshakeMessage>() {

    override fun decode(packet: PacketReader): UpdateHandshakeMessage {
        val major = packet.readInt()
        return UpdateHandshakeMessage(major)
    }

}
