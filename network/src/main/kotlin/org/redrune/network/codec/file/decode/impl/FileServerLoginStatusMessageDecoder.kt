package org.redrune.network.codec.file.decode.impl

import org.redrune.network.codec.file.decode.FileServerMessageDecoder
import org.redrune.network.codec.game.decode.GameMessageDecoder
import org.redrune.network.codec.file.decode.message.FileServerLoginStatusMessage
import org.redrune.network.packet.PacketMetaData
import org.redrune.network.packet.access.PacketReader
import org.redrune.tools.constants.FileServerOpcodes.STATUS_LOGGED_IN
import org.redrune.tools.constants.FileServerOpcodes.STATUS_LOGGED_OUT

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
@PacketMetaData(opcodes = [STATUS_LOGGED_IN, STATUS_LOGGED_OUT], length = 3)
class FileServerLoginStatusMessageDecoder : FileServerMessageDecoder<FileServerLoginStatusMessage>() {
    override fun decode(packet: PacketReader): FileServerLoginStatusMessage {
        val value = packet.readMedium()
        return FileServerLoginStatusMessage(packet.opcode == STATUS_LOGGED_IN, value)
    }
}