package org.redrune.network.codec.file.encode.impl

import io.netty.buffer.Unpooled
import org.redrune.network.codec.file.encode.FileServerMessageEncoder
import org.redrune.network.codec.game.encode.GameMessageEncoder
import org.redrune.network.codec.file.encode.message.FileServerResponseMessage
import org.redrune.network.packet.access.PacketBuilder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class FileServerResponseMessageEncoder : FileServerMessageEncoder<FileServerResponseMessage>() {

    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    override fun encode(builder: PacketBuilder, msg: FileServerResponseMessage) {
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