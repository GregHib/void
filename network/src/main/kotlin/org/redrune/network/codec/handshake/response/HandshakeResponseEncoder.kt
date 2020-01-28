package org.redrune.network.codec.handshake.response

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import mu.KotlinLogging
import org.redrune.network.codec.handshake.request.HandshakeRequestType
import org.redrune.network.codec.handshake.request.HandshakeRequestType.*
import org.redrune.network.codec.handshake.response.HandshakeResponseType.*
import org.redrune.tools.constants.NetworkConstants
import org.redrune.tools.constants.NetworkConstants.Companion.GRAB_SERVER_KEYS

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 27, 2020
 */
class HandshakeResponseEncoder : MessageToByteEncoder<HandshakeResponseMessage>() {
    private val logger = KotlinLogging.logger {}

    override fun encode(ctx: ChannelHandlerContext, msg: HandshakeResponseMessage, out: ByteBuf) {
        ctx.pipeline().remove(this)
        println("Encoding msg $msg")
        val out = Unpooled.buffer()
        when (val type = msg.requestType) {
            UPDATE -> {
                out.writeByte(SUCCESSFUL.opcode)
                GRAB_SERVER_KEYS.forEach { out.writeInt(it) }
            }
            else -> {
                logger.warn { "Unable to encode handshake response, unexpected[type=$type]" }
            }
        }
        ctx.writeAndFlush(out)
    }
}