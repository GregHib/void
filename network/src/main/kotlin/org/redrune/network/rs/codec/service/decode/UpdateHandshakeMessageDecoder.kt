package org.redrune.network.rs.codec.service.decode

import org.redrune.core.network.codec.packet.access.PacketReader
import org.redrune.core.network.model.packet.PacketMetaData
import org.redrune.network.rs.codec.service.ServiceMessageDecoder
import org.redrune.network.rs.codec.service.decode.message.UpdateHandshakeMessage
import org.redrune.utility.constants.network.ServiceOpcodes.FILE_SERVICE

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
