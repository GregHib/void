package org.redrune.network.codec.service.handle.impl

import io.netty.channel.ChannelHandlerContext
import org.redrune.network.codec.login.LoginCodec
import org.redrune.network.codec.login.encode.message.LoginServiceResponseMessage
import org.redrune.network.codec.service.decode.message.LoginServiceHandshakeMessage
import org.redrune.network.codec.service.handle.ServiceMessageHandler
import org.redrune.network.message.codec.rs.RSMessageDecoder
import org.redrune.network.message.codec.ChannelMessageHandler
import org.redrune.network.message.codec.simple.SimpleMessageEncoder
import org.redrune.network.packet.codec.impl.SimplePacketDecoder
import org.redrune.network.replace

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class LoginServiceHandshakeMessageHandler : ServiceMessageHandler<LoginServiceHandshakeMessage>() {

    override fun handle(ctx: ChannelHandlerContext, msg: LoginServiceHandshakeMessage) {
        val pipeline = ctx.pipeline()
        // TODO translate into build function
        pipeline.apply {
            replace("packet.decoder", SimplePacketDecoder(LoginCodec))
            replace("message.decoder", RSMessageDecoder(LoginCodec))
            replace("message.handler",
                ChannelMessageHandler(LoginCodec)
            )
            replace("message.encoder", SimpleMessageEncoder(LoginCodec))
        }

        ctx.pipeline().writeAndFlush(LoginServiceResponseMessage(0))
    }

}