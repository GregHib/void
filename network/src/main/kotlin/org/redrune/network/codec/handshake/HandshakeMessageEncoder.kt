package org.redrune.network.codec.handshake

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.redrune.network.Session
import org.redrune.network.codec.update.CacheArchiveEncoder
import org.redrune.network.codec.update.UpdateDecoder
import org.redrune.network.codec.update.UpdateSession
import org.redrune.tools.constants.NetworkConstants

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 1:27 a.m.
 */
class HandshakeMessageEncoder  : MessageToByteEncoder<HandshakeResponse>() {
    override fun encode(ctx: ChannelHandlerContext, msg: HandshakeResponse, out: ByteBuf) {
        println("encoding $msg")
        val response = Unpooled.buffer()
        out.writeByte(msg.responseValue)
        if (msg.responseValue == HandshakeResponseValue.SUCCESSFUL) {
            NetworkConstants.GRAB_SERVER_KEYS.forEach { out.writeInt(it) }

            ctx.channel().attr(Session.SESSION_KEY).set(UpdateSession(ctx.channel()))
            ctx.pipeline().remove(this)
            ctx.pipeline().addFirst(CacheArchiveEncoder(), UpdateDecoder())
        }

    }

}