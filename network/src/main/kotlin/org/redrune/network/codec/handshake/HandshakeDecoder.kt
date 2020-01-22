package org.redrune.network.codec.handshake

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.redrune.network.codec.handshake.model.HandshakeMessage
import org.redrune.network.codec.message.GameMessageDecoder
import org.redrune.network.codec.message.GameMessageEncoder
import org.redrune.tools.constants.NetworkConstants

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
class HandshakeDecoder : ByteToMessageDecoder() {
    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (!buf.isReadable) return

        val pipeline = ctx.pipeline().remove(this)
        val serviceOpcode = buf.readUnsignedByte().toInt()

        println("serviceOpcode=$serviceOpcode")
        when (serviceOpcode) {
            HandshakeMessage.JS5_REQUEST_OPCODE -> {
                val version = buf.readInt()
                if (version == NetworkConstants.PROTOCOL_NUMBER) {
                    pipeline.addFirst(GameMessageEncoder(), GameMessageDecoder())
                } else {

                }
            }
            HandshakeMessage.LOGIN_REQUEST_OPCODE -> {
//                pipeline.addBefore("handler", "packet.decoder", LoginRequestDecoder())
            }
        }
        out.add(HandshakeMessage(serviceOpcode))
    }
}