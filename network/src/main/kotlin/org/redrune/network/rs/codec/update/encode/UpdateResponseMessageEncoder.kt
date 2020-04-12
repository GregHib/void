package org.redrune.network.rs.codec.update.encode

import io.netty.buffer.Unpooled
import org.redrune.core.network.codec.packet.access.PacketWriter
import org.redrune.network.rs.codec.update.UpdateMessageEncoder
import org.redrune.network.rs.codec.update.encode.message.UpdateResponseMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class UpdateResponseMessageEncoder : UpdateMessageEncoder<UpdateResponseMessage>() {

    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    override fun encode(builder: PacketWriter, msg: UpdateResponseMessage) {
        val (indexId, archiveId, data, compression, length, attributes) = msg

        val buffer = Unpooled.buffer()

        buffer.writeByte(indexId)
        buffer.writeShort(archiveId)
        buffer.writeByte(attributes)
        buffer.writeInt(length)

        val realLength = if (compression != 0) length + 4 else length
        for (offset in 5 until realLength + 5) {
            if (buffer.writerIndex() % 512 === 0) {
                buffer.writeByte(255)
            }
            buffer.writeByte(data[offset].toInt())
        }
        builder.writeBytes(buffer)
    }

}