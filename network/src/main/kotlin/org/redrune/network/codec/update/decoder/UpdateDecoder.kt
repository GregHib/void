package org.redrune.network.codec.update.decoder

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.redrune.network.codec.update.message.UpdateRequestType
import org.redrune.network.codec.update.message.impl.ClientVersionMessage
import org.redrune.network.codec.update.message.impl.FileRequestMessage

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
class UpdateDecoder : ByteToMessageDecoder() {

    private var state =
        DecodeState.DECODE_VERSION

    private val logger = InlineLogger()

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        logger.info { "Decoding msg $buf at state $state" }
        when (state) {
            DecodeState.DECODE_VERSION -> {
                val version = buf.readUnsignedInt().toInt()
                logger.info { "decoded version=$version, remaining=${buf.readableBytes()}" }
                out.add(ClientVersionMessage(version))
                state =
                    DecodeState.DECODE_FILE_REQUEST
            }
            DecodeState.DECODE_FILE_REQUEST -> {
                val opcode: Int = buf.readUnsignedByte().toInt()
                val type =
                    UpdateRequestType.valueOf(opcode)
                logger.info { "opcode=$opcode, type=$type, readable=${buf.readableBytes()}" }
                if (type == null) {
                    logger.warn { "Unexpected file request type! [opcode=$opcode]" }
                    return
                }
                when (type) {
                    UpdateRequestType.FILE_REQUEST, UpdateRequestType.PRIORITY_FILE_REQUEST -> {
                        val indexId = buf.readUnsignedByte().toInt()
                        val archiveId = buf.readUnsignedShort()
                        out.add(
                            FileRequestMessage(
                                indexId,
                                archiveId,
                                type == UpdateRequestType.PRIORITY_FILE_REQUEST
                            )
                        )
                    }
                    UpdateRequestType.ENCRYPTION -> {
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
        }
    }

    private enum class DecodeState { DECODE_VERSION, DECODE_FILE_REQUEST }
}