package rs.dusk.network.rs.codec.update.decode

import rs.dusk.core.io.DataType
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.network.rs.codec.update.FileServerOpcodes.FILE_REQUEST
import rs.dusk.network.rs.codec.update.FileServerOpcodes.PRIORITY_FILE_REQUEST
import rs.dusk.network.rs.codec.update.UpdateMessageDecoder
import rs.dusk.network.rs.codec.update.decode.message.UpdateRequestMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
@PacketMetaData(opcodes = [FILE_REQUEST, PRIORITY_FILE_REQUEST], length = 3)
class UpdateRequestMessageDecoder : UpdateMessageDecoder<UpdateRequestMessage>() {

    override fun decode(packet: PacketReader): UpdateRequestMessage {
        val hash = packet.readUnsigned(DataType.MEDIUM)
        val indexId = (hash shr 16).toInt()
        val archiveId = (hash and 0xffff).toInt()
        return UpdateRequestMessage(indexId, archiveId, packet.opcode == PRIORITY_FILE_REQUEST)
    }

}