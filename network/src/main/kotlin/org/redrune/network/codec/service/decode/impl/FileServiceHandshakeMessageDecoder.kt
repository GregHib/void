package org.redrune.network.codec.service.decode.impl

import org.redrune.network.codec.service.decode.ServiceMessageDecoder
import org.redrune.network.codec.service.decode.message.FileServiceHandshakeMessage
import org.redrune.network.packet.PacketMetaData
import org.redrune.network.packet.access.PacketReader
import org.redrune.tools.constants.ServiceOpcodes
import org.redrune.tools.constants.ServiceOpcodes.FILE_SERVICE

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
@PacketMetaData(opcodes = [FILE_SERVICE], length=4)
class FileServiceHandshakeMessageDecoder : ServiceMessageDecoder<FileServiceHandshakeMessage>() {
    override fun decode(packet: PacketReader): FileServiceHandshakeMessage {
        val major = packet.readInt()
        return FileServiceHandshakeMessage(major)
    }
}
