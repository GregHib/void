package org.redrune.network.model.message

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.redrune.network.codec.Codec
import org.redrune.network.model.packet.PacketBuilder
import java.io.IOException

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
class OutboundSimpleMessageEncoder(codec: Codec) : OutboundMessageEncoder(codec) {

    private val logger = InlineLogger()
}