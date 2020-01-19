package org.redrune.network.codec.handshake

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.redrune.network.NetworkConstants
import org.redrune.network.NetworkSession
import org.redrune.network.codec.js5.FileRequestDecoder
import org.redrune.network.codec.login.LoginRequestDecoder
import org.redrune.network.packet.PacketConstants
import org.redrune.network.packet.struct.OutgoingPacket
import kotlin.experimental.and

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-07
 */
class HandshakeDecoder : ByteToMessageDecoder() {
    override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
        val session: NetworkSession? = ctx.channel().attr(NetworkConstants.SESSION_KEY).get()
        val pipeline = ctx.pipeline().remove(this)
        val id: Byte = `in`.readByte() and 0xFF.toByte()
        val packet = OutgoingPacket(-1)
        session?.state = NetworkSession.SessionState.HANDSHAKE
        when (id) {
            PacketConstants.JS5_REQUEST_OPCODE -> {
                val version = `in`.readInt()
                if (version == NetworkConstants.PROTOCOL_NUMBER) {
                    packet.writeByte(0)
                    for (i in 0..26) {
                        packet.writeInt(NetworkConstants.GRAB_SERVER_KEYS[i])
                    }
                    pipeline.addBefore("handler", "decoder", FileRequestDecoder())
                } else {
                    packet.writeByte(6)
                }
            }
            PacketConstants.LOGIN_REQUEST_OPCODE -> {
                packet.writeByte(0)
                pipeline.addBefore("handler", "decoder", LoginRequestDecoder())
            }
        }
        ctx.writeAndFlush(packet.buffer)
    }
}