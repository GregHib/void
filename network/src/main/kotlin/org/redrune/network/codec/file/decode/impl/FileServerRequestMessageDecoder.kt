package org.redrune.network.codec.file.decode.impl

import org.redrune.network.codec.file.decode.FileServerMessageDecoder
import org.redrune.network.codec.game.decode.GameMessageDecoder
import org.redrune.network.codec.file.decode.message.FileServerRequestMessage
import org.redrune.network.packet.PacketMetaData
import org.redrune.network.packet.DataType
import org.redrune.network.packet.access.PacketReader
import org.redrune.tools.constants.FileServerOpcodes.FILE_REQUEST
import org.redrune.tools.constants.FileServerOpcodes.PRIORITY_FILE_REQUEST

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
@PacketMetaData(opcodes = [FILE_REQUEST, PRIORITY_FILE_REQUEST], length = 3)
class FileServerRequestMessageDecoder : FileServerMessageDecoder<FileServerRequestMessage>() {

    override fun decode(packet: PacketReader): FileServerRequestMessage {
        val hash = packet.readUnsigned(DataType.MEDIUM)
        val indexId = (hash shr 16).toInt()
        val archiveId = (hash - (indexId shl 16)).toInt()
        return FileServerRequestMessage(indexId, archiveId, packet.opcode == PRIORITY_FILE_REQUEST)
    }

}