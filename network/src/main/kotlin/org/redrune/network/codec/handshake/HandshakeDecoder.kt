package org.redrune.network.codec.handshake

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.redrune.network.NetworkSession
import org.redrune.network.codec.fs.FileRequestDecoder
import org.redrune.network.codec.handshake.decode.message.HandshakeMessage
import org.redrune.network.codec.login.decode.LoginRequestDecoder
import org.redrune.network.packet.PacketWriter
import org.redrune.tools.constants.PacketConstants
import org.redrune.tools.constants.NetworkConstants
import kotlin.experimental.and

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
class HandshakeDecoder : ByteToMessageDecoder() {
    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        println("HandshakeDecoder.decode")

        val session: NetworkSession? = ctx.channel().attr(NetworkSession.SESSION_KEY).get()
        val pipeline = ctx.pipeline().remove(this)
        val id: Byte = buf.readByte() and 0xFF.toByte()
        session?.state = NetworkSession.SessionState.HANDSHAKE
        val handshakeMessage = HandshakeMessage(0)
        when (id) {
            PacketConstants.JS5_REQUEST_OPCODE -> {
                val version = buf.readInt()
                if (version == NetworkConstants.PROTOCOL_NUMBER) {
                    pipeline.addBefore("message.decoder", "packet.decoder", FileRequestDecoder())
                } else {
                    handshakeMessage.response = 6
                }
            }
            PacketConstants.LOGIN_REQUEST_OPCODE -> {
                pipeline.addBefore("handler", "packet.decoder", LoginRequestDecoder())
            }
        }
        ctx.channel().writeAndFlush(handshakeMessage)
    }
}