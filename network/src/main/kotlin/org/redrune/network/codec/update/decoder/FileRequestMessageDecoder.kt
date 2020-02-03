package org.redrune.network.codec.update.decoder

import org.redrune.network.codec.update.UpdateOpcodes.FILE_REQUEST
import org.redrune.network.codec.update.UpdateOpcodes.PRIORITY_FILE_REQUEST
import org.redrune.network.codec.update.message.UpdateRequestType
import org.redrune.network.codec.update.message.impl.FileRequestMessage
import org.redrune.network.model.message.Message
import org.redrune.network.model.message.MessageDecoder
import org.redrune.network.model.packet.PacketReader

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
class FileRequestMessageDecoder : MessageDecoder(intArrayOf(FILE_REQUEST, PRIORITY_FILE_REQUEST), 3) {
    override fun decode(reader: PacketReader): Message {
        val indexId = reader.readUnsignedByte()
        val archiveId = reader.readUnsignedShort()
        val type = UpdateRequestType.valueOf(reader.opcode)
        return FileRequestMessage(indexId, archiveId, type == UpdateRequestType.PRIORITY_FILE_REQUEST)
    }
}