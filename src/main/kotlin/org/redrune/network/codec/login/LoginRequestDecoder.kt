package org.redrune.network.codec.login

import UTIL.buffer.FixedBuffer
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.redrune.network.NetworkConstants
import org.redrune.network.NetworkSession
import org.redrune.network.packet.outgoing.LoginResponsePacket
import org.redrune.util.LoginReturnCode

class LoginRequestDecoder : ByteToMessageDecoder() {

    private var session: NetworkSession? = null
        set(value) {
            field = value
            field?.channel?.attr(NetworkConstants.SESSION_KEY)?.set(field);
        }

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        if (buf.readableBytes() < 3) {
            return
        }
        val opcode = buf.readUnsignedByte().toInt()
        val size = buf.readUnsignedShort()
        if (buf.readableBytes() != size) {
            ctx.close()
            return
        }
        if (opcode != 16 && opcode != 18 && opcode != 19) {
            println("Received unexpected world login opcode: $opcode")
            session = NetworkSession(ctx.channel())
            session?.write(LoginResponsePacket(LoginReturnCode.BETA_TESTERS_ONLY))
                ?.addListener(ChannelFutureListener.CLOSE)
            return
        }
        val revision = buf.readInt()
        if (revision != NetworkConstants.PROTOCOL_NUMBER) {
            println("Received unexpected protocol number: $revision")
            session = NetworkSession(ctx.channel())
            session?.write(LoginResponsePacket(LoginReturnCode.BETA_TESTERS_ONLY))
                ?.addListener(ChannelFutureListener.CLOSE)
            return
        }
        session = NetworkSession(ctx.channel())
        val data = ByteArray(size - 4)
        // store the data into the buffer
        // store the data into the buffer
        buf.readBytes(data)
        // convert the buffer into a readable object
        // convert the buffer into a readable object
        val buffer = FixedBuffer(data)
        session?.write(LoginResponsePacket(LoginReturnCode.ACCOUNT_DISABLED))
            ?.addListener(ChannelFutureListener.CLOSE)
        println("$session, ${session?.channel}")
        when (opcode) {
            19 -> {
                println("decode lobby!!")
                //            decodeLobbyLogin(ctx, buffer, out)
            }
            16 -> {
                println("Decode world!!")
                //            decodeWorldLogin(ctx, buffer, out)
            }
            else -> {
                println("Received unexpected login request from $session. [opcode=$opcode]")
                ctx.channel().close()
            }
        }
    }

}