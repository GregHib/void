package org.redrune.network.codec.update

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import mu.KotlinLogging
import org.redrune.network.codec.update.message.FileRequestMessage
import org.redrune.network.codec.update.message.FileRequestType

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 25, 2020 6:45 p.m.
 */
class UpdateDecoder : ByteToMessageDecoder() {

    private val logger = KotlinLogging.logger {}

    @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        val bldr = StringBuilder()
        ByteBufUtil.appendPrettyHexDump(bldr, buf)
        print("\n$bldr\n")
        if (buf.readableBytes() < 4)
            return

        val opcode: Int = buf.readUnsignedByte().toInt()
        println("opcode=$opcode")
        val type = FileRequestType.valueOf(opcode)
        if (type == null) {
            logger.warn { "Unexpected file request type! [opcode=$opcode]" }
            return
        }
        println("type=$type")
        when (type) {
            FileRequestType.FILE_REQUEST, FileRequestType.PRIORITY_FILE_REQUEST -> {
                val indexId = buf.readUnsignedByte().toInt()
                val archiveId = buf.readUnsignedShort()
                out.add(FileRequestMessage(indexId, archiveId, type == FileRequestType.PRIORITY_FILE_REQUEST))
            }
            FileRequestType.ENCRYPTION -> {
                val key = buf.readUnsignedByte().toInt()
                val mark = buf.readUnsignedShort()
                logger.info { "Received file encryption info [key=$key, mark=$mark]"}
            }
            else -> {
                buf.readerIndex(buf.readerIndex() + 3);
                logger.info { "Unhandled file request type $type" }
            }
        }
    }

    private enum class State { READ_VERSION, READ_REQUEST }
}