package org.redrune.network.codec.file

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import org.redrune.network.model.packet.PacketReader

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-31
 */
class UpdateDecoder : MessageToMessageDecoder<PacketReader>() {

    private var state = DecodeState.DECODE_VERSION

    private val logger = InlineLogger()

    override fun decode(ctx: ChannelHandlerContext, msg: PacketReader, out: MutableList<Any>) {
        logger.info { "DEcoding msg $ msg" }
        when (state) {
            DecodeState.DECODE_VERSION -> {
                val version = msg.readUnsignedInt()

                logger.info { "decoded version=$version" }
                state = DecodeState.DECODE_FILE_REQUEST
            }
            DecodeState.DECODE_FILE_REQUEST -> {

            }
        }
    }

    private enum class DecodeState { DECODE_VERSION, DECODE_FILE_REQUEST }
}