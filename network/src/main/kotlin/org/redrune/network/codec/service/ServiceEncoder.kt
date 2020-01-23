package org.redrune.network.codec.service

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.redrune.network.codec.handshake.HandshakeDecoder
import org.redrune.network.codec.handshake.HandshakeEncoder
import org.redrune.network.session.HandshakeSession
import org.redrune.network.session.Session

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020 12:41 a.m.
 */
class ServiceEncoder : MessageToByteEncoder<ServiceResponse>()  {
    override fun encode(ctx: ChannelHandlerContext, msg: ServiceResponse, out: ByteBuf) {
        ctx.channel().attr<Session>(Session.SESSION_KEY).set(HandshakeSession(ctx.channel()))

        when(msg.service) {
            Service.FILE_SERVICE ->{
                ctx.pipeline().apply {
                    replace("service.encoder", "handshake.encoder", HandshakeEncoder())
                    replace("service.decoder", "handshake.decoder", HandshakeDecoder())
                }
            }
            Service.LOGIN_SERVICE -> {

            }
        }

    }
}