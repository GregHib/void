package org.redrune.network.codec.update.encode

import com.google.common.primitives.Ints
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.redrune.cache.Cache
import org.redrune.network.codec.update.message.FileRequestMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 25, 2020 6:51 p.m.
 */
class FileRequestEncoder : MessageToByteEncoder<FileRequestMessage>() {
    override fun encode(ctx: ChannelHandlerContext, msg: FileRequestMessage, out: ByteBuf) {
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