package org.redrune.network.codec.update.encoder

import com.google.common.primitives.Ints
import io.netty.buffer.Unpooled
import org.redrune.cache.Cache
import org.redrune.network.codec.update.message.impl.FileRequestMessage
import org.redrune.network.model.message.MessageEncoder
import org.redrune.network.model.packet.PacketBuilder

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
class FileResponseEncoder : MessageEncoder<FileRequestMessage>() {

    override fun encode(out: PacketBuilder, msg: FileRequestMessage) {
        println("encoding msg=$msg")
        val (indexId, archiveId, priority) = msg
        val data = Cache.getFile(indexId, archiveId) ?: return
        val compression: Int = data[0].toInt() and 0xff
        val length = Ints.fromBytes(data[1], data[2], data[3], data[4])
        var attributes = compression
        if (!priority) {
            attributes = attributes or 0x80
        }
        val outBuffer = Unpooled.buffer()
        outBuffer.writeByte(indexId)
        outBuffer.writeShort(archiveId)
        outBuffer.writeByte(attributes)
        outBuffer.writeInt(length)
        val realLength = if (compression != 0) length + 4 else length
        for (offset in 5 until realLength + 5) {
            if (outBuffer.writerIndex() % 512 === 0) {
                outBuffer.writeByte(255)
            }
            outBuffer.writeByte(data[offset].toInt())
        }
        out.writeBytes(outBuffer)
    }
}