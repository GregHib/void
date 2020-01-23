package org.redrune.network.codec.file

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.redrune.network.codec.file.FileRequestType.*

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
// TODO: efficiently deliver requests, reference apollo!
class FileRequestDecoder : ByteToMessageDecoder() {

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (!buf.isReadable) {
            println("buf not readable")
            return
        }
        do {
            val opcode = buf.readUnsignedByte().toInt()
            println("requesting opcode $opcode")

            when (val request = values().first { it.opcode == opcode }) {
                FILE_REQUEST, PRIORITY_FILE_REQUEST -> {
                    val indexId: Int = buf.readUnsignedByte().toInt()
                    val archiveId: Int = buf.readUnsignedShort()
                    out.add(FileRequest(indexId, archiveId, request.priority))
                }
                // TODO test encryption
                ENCRYPTION -> {
                    val value: Int = buf.readUnsignedByte().toInt()
                    val mark = buf.readUnsignedShort()
                    out.add(FileEncryptionKey(value, mark))
                    println("encrypt")
                }
                // TODO find if this is in 667
                CLIENT_LOGGED_IN, CLIENT_LOGGED_OUT, CONNECTION_INITIATED, CONNECTION_TERMINATED -> {
                    buf.readerIndex(buf.readerIndex() + 3)
                    println("request=$request")
                }
            }
        } while(buf.readableBytes() >= 4)
    }

}