package rs.dusk.network.rs.codec.update.decode

import rs.dusk.buffer.DataType
import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.network.rs.codec.update.UpdateMessageDecoder
import rs.dusk.network.rs.codec.update.decode.message.UpdateRequestMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class UpdateRequestMessageDecoder(private val priority: Boolean) : UpdateMessageDecoder<UpdateRequestMessage>(3) {

    override fun decode(packet: PacketReader): UpdateRequestMessage {
        val hash = packet.readUnsigned(DataType.MEDIUM)
        val indexId = (hash shr 16).toInt()
        val archiveId = (hash and 0xffff).toInt()
        return UpdateRequestMessage(indexId, archiveId, priority)
    }

}