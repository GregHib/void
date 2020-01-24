package org.redrune.network.codec.handshake

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import mu.KotlinLogging
import org.redrune.network.codec.handshake.ServiceType.Companion.SERVICE_LOGIN
import org.redrune.network.codec.handshake.ServiceType.Companion.SERVICE_UPDATE

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
class HandshakeDecoder : ByteToMessageDecoder() {

    private val logger = KotlinLogging.logger {}

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (!buf.isReadable) return
        val opcode = buf.readUnsignedByte().toInt()

        when (opcode) {
            SERVICE_UPDATE -> {
                val version = buf.readInt()
                ctx.pipeline().addFirst(HandshakeMessageEncoder())
                out.add(HandshakeMessage(version))
            }
            SERVICE_LOGIN -> {

            }
            else -> {
                logger.warn { "Unable to identify service by [opcode=$opcode]" }
            }
        }
        ctx.pipeline().remove(this)
    }
}