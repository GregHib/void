package org.redrune.network.codec.update

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import mu.KotlinLogging
import org.redrune.network.Session
import org.redrune.network.codec.update.FileRequestType.*

/**
 * This class decodes the update protocol into its version different messages
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
// TODO: efficiently deliver requests, reference apollo!
class UpdateDecoder : ByteToMessageDecoder() {

    private val logger = KotlinLogging.logger {}

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (!buf.isReadable) {
            error("buf not readable")
        }
        if (buf.readableBytes() < 4) {
            error("not enough readable bytes")
        }
        val opcode = buf.readUnsignedByte().toInt()
        println("opcode=$opcode")
        val requestType = values().firstOrNull() { it.opcode == opcode }
        if (requestType == null) {
            logger.warn { "Unable to identify cache request type with [opcode=$opcode]" }
        }
        logger.info { "UpdateRequest=$requestType(${buf.readableBytes()})" }

        when (requestType) {
            FILE_REQUEST, PRIORITY_FILE_REQUEST -> {
                val indexId: Int = buf.readUnsignedByte().toInt()
                val archiveId: Int = buf.readUnsignedShort()
                out.add(
                    FileRequest(
                        indexId,
                        archiveId,
                        requestType.priority
                    )
                )
            }

            ENCRYPTION -> {
                val value: Int = buf.readUnsignedByte().toInt()
                val mark = buf.readUnsignedShort()
                ctx.channel().attr(Session.ENCRYPTION_KEY).set(value)
                if (mark != 0) {
                    logger.info("Invalid decryption packet $mark")
                    ctx.close()
                }
            }

            CLIENT_LOGGED_IN, CLIENT_LOGGED_OUT -> {
                buf.readerIndex(buf.readerIndex() + 3)
            }

            CONNECTION_INITIATED, CONNECTION_TERMINATED -> {
                buf.readerIndex(buf.readerIndex() + 3)
            }
        }
    }

}