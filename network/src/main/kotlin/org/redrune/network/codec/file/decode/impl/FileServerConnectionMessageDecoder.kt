package org.redrune.network.codec.file.decode.impl

import org.redrune.network.codec.file.decode.FileServerMessageDecoder
import org.redrune.network.codec.game.decode.GameMessageDecoder
import org.redrune.network.codec.file.decode.message.FileServerConnectionMessage
import org.redrune.network.packet.PacketMetaData
import org.redrune.network.packet.access.PacketReader
import org.redrune.tools.constants.FileServerOpcodes.CONNECTED

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
@PacketMetaData(opcodes = [CONNECTED], length = 3)
class FileServerConnectionMessageDecoder : FileServerMessageDecoder<FileServerConnectionMessage>() {
    override fun decode(packet: PacketReader): FileServerConnectionMessage {
        val value = packet.readMedium()
        return FileServerConnectionMessage(value)
    }

}