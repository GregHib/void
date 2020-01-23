package org.redrune.network.codec.file

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.redrune.cache.Cache
import org.redrune.network.session.Session.Companion.ENCRYPTION_KEY

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 22, 2020
 */
class FileResponseEncoder : MessageToByteEncoder<FileResponse>() {
    override fun encode(ctx: ChannelHandlerContext, msg: FileResponse, out: ByteBuf) {
        val indexId = msg.indexId
        val archiveId = msg.archiveId

        println("to encode response $msg")
        //Check index and archive exist
        if (archiveId < 0) {
            return
        }
        if (indexId != 255) {
            if (Cache.indexes.lastIndex <= indexId || Cache.indexes[indexId] == null || !Cache.indexes[indexId].archiveExists(archiveId)) {
                return
            }
        } else if (archiveId != 255) {
            if (Cache.indexes.lastIndex <= archiveId || Cache.indexes[archiveId] == null) {
                return
            }
        }
        //Retrieve cache data
        val data: ByteArray =
            (if (indexId == 255) Cache.index255 else Cache.indexes[indexId].mainFile).getArchiveData(archiveId)
                ?: return
        //Retrieve encryption key
        val encryption =
            if (indexId == 255 && archiveId == 255 || !ctx.channel().hasAttr(ENCRYPTION_KEY)) 0 else ctx.channel().attr(
                ENCRYPTION_KEY
            ).get()
        //Read compression key
        val compression = data[0].toInt() and 0xff
        //Read data length
        val length =
            ((data[1].toInt() and 0xff shl 24) + (data[2].toInt() and 0xff shl 16) + (data[3].toInt() and 0xff shl 8) + (data[4].toInt() and 0xff))
        //Mark as non-priority
        var settings = compression
        if (!msg.priority) {
            settings = settings or 0x80
        }
        //Calculate uncompress size
        val size = if (compression == 0) length else length + 4
        //Send file
//        ctx.channel().write(FileContents(indexId, archiveId, settings, length, data, size, encryption))

        val buf = Unpooled.buffer()
        val start = buf.writerIndex()
        //Write header
        buf.writeByte(indexId)
        buf.writeShort(archiveId)
        buf.writeByte(settings)
        buf.writeInt(length)
        val realLength = if (compression != 0) length + 4 else length
        for (index in 5 until realLength + 5) {
            if (buf.writerIndex() % 512 === 0) {
                buf.writeByte(255)
            }
            buf.writeByte(data[index].toInt())
        }

        //Write data split into chunks of 512
        encode(buf, data, size, 5, 512)

        //Write encryption value if present
        if (encryption != 0) {
            for (i in start until buf.arrayOffset()) {
                buf.setByte(i, buf.getByte(i).toInt() xor encryption)
            }
        }
        //Send file
        out.writeBytes(buf)
//        ctx.channel().writeAndFlush(buf)
    }

    fun encode(builder: ByteBuf, data: ByteArray, length: Int, dataHeader: Int, chunkSize: Int) {
        //Calculate the last index after writing
        val endIndex = length + dataHeader
        //Calculate the number of chunks to split the data into
        val chunks = Math.ceil((builder.writerIndex() + length) / (chunkSize - 1.0)).toInt()
        //Offset by the data header
        var offset = dataHeader
        val startOffset = Math.floor(builder.writerIndex() / chunkSize.toDouble()).toInt()
        //For every chunk (inclusive)
        for (i in startOffset..startOffset + chunks) {
            //Break if at the end of the data (empty chunk)
            if (offset == endIndex) {
                break
            }
            //If at a chunk; write a delimiter
            if (builder.writerIndex() % chunkSize == 0) {
                builder.writeByte(-1)
            }
            //The start of the chunk
            val start = builder.writerIndex()
            //The next chunk index
            val end = (i + 1) * chunkSize
            //Amount of space available to write to in this chunk
            val availableSize = end - start
            //Amount of data to write, capped at max available
            val size = Math.min(availableSize, endIndex - offset)
            //Write bytes
            builder.writeBytes(data, offset, size)
            //Increase offset by amount written
            offset += size
        }
    }
}