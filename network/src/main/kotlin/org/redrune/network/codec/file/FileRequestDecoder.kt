package org.redrune.network.codec.file

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.redrune.network.codec.file.FileRequestType.*

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
class FileRequestDecoder : ByteToMessageDecoder() {

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (buf.readableBytes() < 4 || !buf.isReadable) return
        val opcode = buf.readUnsignedByte().toInt()
        println("requesting opcode $opcode")

        when (val request = values().first { it.opcode == opcode }) {
            FILE_REQUEST, PRIORITY_FILE_REQUEST -> {
                val indexId: Int = buf.readUnsignedByte().toInt()
                val archiveId = buf.readUnsignedShort()
                println("yo dis!! [$request]")
                out.add(FileRequest(indexId, archiveId, request.priority))
            }
            ENCRYPTION -> {
                val value: Int = buf.readUnsignedByte().toInt()
                val mark = buf.readUnsignedShort()
                out.add(FileEncryptionKey(value, mark))
                println("value=$value, mark=$mark")
            }
            CLIENT_LOGGED_IN, CLIENT_LOGGED_OUT, CONNECTION_INITIATED, CONNECTION_TERMINATED -> {
//                buf.readerIndex(buf.readerIndex() + 3)
//                println("request=$request")
            }
        }
    }

}