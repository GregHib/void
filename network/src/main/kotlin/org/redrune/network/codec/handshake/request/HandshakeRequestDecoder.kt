package org.redrune.network.codec.handshake.request

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import mu.KotlinLogging
import org.redrune.network.Session


/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 25, 2020 3:57 p.m.
 */
class HandshakeRequestDecoder : ByteToMessageDecoder() {

    private val logger = KotlinLogging.logger {}

    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
        if (!msg.isReadable) {
            return
        }
        val opcode = msg.readUnsignedByte().toInt()
        val version = msg.readUnsignedInt().toInt()
        ctx.pipeline().remove(this)
        val type = HandshakeRequestType.valueOf(opcode)
        if (type == null) {
            logger.warn { "Unable to identify expected service! [opcode=$opcode]" }
        } else {
            out.add(HandshakeRequestMessage(type, version))
            logger.info { "Service $type requested, passing to handler " }
        }
    }


}