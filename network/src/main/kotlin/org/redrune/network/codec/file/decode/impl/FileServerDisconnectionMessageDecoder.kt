package org.redrune.network.codec.file.decode.impl

import org.redrune.network.codec.file.decode.FileServerMessageDecoder
import org.redrune.network.codec.game.decode.GameMessageDecoder
import org.redrune.network.codec.file.decode.message.FileServerDisconnectionMessage
import org.redrune.network.packet.PacketMetaData
import org.redrune.network.packet.access.PacketReader
import org.redrune.tools.constants.FileServerOpcodes.DISCONNECTED

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
@PacketMetaData(opcodes = [DISCONNECTED], length = 3)
class FileServerDisconnectionMessageDecoder : FileServerMessageDecoder<FileServerDisconnectionMessage>() {
    override fun decode(packet: PacketReader): FileServerDisconnectionMessage {
        val value = packet.readMedium()
        return FileServerDisconnectionMessage(value)
    }
}