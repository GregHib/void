package org.redrune.network.codec.update

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.ReplayingDecoder
import io.netty.handler.codec.base64.Base64
import mu.KotlinLogging
import org.redrune.network.codec.update.message.UpdateVersionMessage
import org.redrune.network.codec.update.UpdateDecoder.State.READ_REQUEST
import org.redrune.network.codec.update.UpdateDecoder.State.READ_VERSION
import org.redrune.network.codec.update.message.FileRequest
import org.redrune.network.codec.update.message.FileRequestType
import java.nio.charset.StandardCharsets

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 25, 2020 6:45 p.m.
 */
class UpdateDecoder : ByteToMessageDecoder() {

    private val logger = KotlinLogging.logger {}

    private var state = READ_VERSION

    @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        val bldr = StringBuilder()
        ByteBufUtil.appendPrettyHexDump(bldr, buf)
        print("$bldr\n")
        if (buf.readableBytes() < 4)
            return

        println("State=$state, buf=$buf")
        if (state == READ_VERSION) {
            val version = buf.readUnsignedInt().toInt()
            println("Version=$version")
            state = READ_REQUEST
            out.add(UpdateVersionMessage(version))
        } else if (state == READ_REQUEST) {
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
                    out.add(FileRequest(indexId, archiveId, type == FileRequestType.PRIORITY_FILE_REQUEST))
                }
                else -> {
                    logger.info { "Unhandled file request type $type" }
                }
            }
        }
    }

    private enum class State { READ_VERSION, READ_REQUEST }
}