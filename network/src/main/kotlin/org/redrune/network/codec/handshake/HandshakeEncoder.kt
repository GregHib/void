package org.redrune.network.codec.handshake

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.redrune.network.codec.file.FileRequestDecoder
import org.redrune.network.codec.file.FileResponseEncoder
import org.redrune.network.session.FileSession
import org.redrune.network.session.Session
import org.redrune.tools.constants.NetworkConstants

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020 1:43 a.m.
 */
class HandshakeEncoder : MessageToByteEncoder<HandshakeResponse>() {
    override fun encode(ctx: ChannelHandlerContext, msg: HandshakeResponse, out: ByteBuf) {
        val state = msg.state
        out.writeByte(state.value)
        if (state != HandshakeState.SUCCESSFUL) {
            return
        }
        NetworkConstants.GRAB_SERVER_KEYS.forEach { out.writeInt(it) }
        ctx.channel().attr<Session>(Session.SESSION_KEY).set(FileSession(ctx.channel()))
        ctx.pipeline().apply {
            replace("handshake.encoder", "file.encoder", FileResponseEncoder())
            replace("handshake.decoder", "file.decoder", FileRequestDecoder())
        }
    }
}