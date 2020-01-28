package org.redrune.network.codec.handshake.request

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import mu.KotlinLogging


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
        var additionalBuf: ByteBuf? = null
        if (msg.isReadable) {
            additionalBuf = msg.readBytes(msg.readableBytes())
        }
        println("opcode=$opcode")
        ctx.pipeline().remove(this)
        val type = HandshakeRequestType.valueOf(opcode)
        if (type == null) {
            logger.warn { "Unable to identify expected service! [opcode=$opcode]" }
        } else {
            out.add(HandshakeRequestMessage(type))
            logger.info { "Service $type requested, passing to handler " }
        }
        val bldr = StringBuilder()
        ByteBufUtil.appendPrettyHexDump(bldr, additionalBuf)
        print("$bldr\n")
    }



}