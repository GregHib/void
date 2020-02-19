package org.redrune.network.codec.service.handle.impl

import io.netty.channel.ChannelHandlerContext
import org.redrune.network.codec.login.LoginCodec
import org.redrune.network.codec.login.encode.impl.LoginServiceResponseMessageEncoder
import org.redrune.network.codec.login.encode.message.LoginServiceResponseMessage
import org.redrune.network.codec.service.decode.message.LoginServiceHandshakeMessage
import org.redrune.network.message.codec.MessageHandler
import org.redrune.network.message.codec.game.GameMessageDecoder
import org.redrune.network.message.codec.game.GameMessageHandler
import org.redrune.network.message.codec.simple.SimpleMessageEncoder
import org.redrune.network.packet.codec.impl.SimplePacketDecoder
import sun.net.www.MessageHeader

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class LoginServiceHandshakeMessageHandler : MessageHandler<LoginServiceHandshakeMessage>() {

    override fun handle(ctx: ChannelHandlerContext, msg: LoginServiceHandshakeMessage) {
        val pipeline = ctx.pipeline()
        // TODO translate into build function
        pipeline.apply {
            replace("packet.decoder", "packet.decoder", SimplePacketDecoder(LoginCodec))
            replace("message.decoder", "message.decoder", GameMessageDecoder(LoginCodec))
            replace("message.handler", "message.handler", GameMessageHandler(LoginCodec))
            replace("message.encoder", "message.encoder", SimpleMessageEncoder(LoginCodec))
        }

        ctx.pipeline().writeAndFlush(LoginServiceResponseMessage(0))
    }

}