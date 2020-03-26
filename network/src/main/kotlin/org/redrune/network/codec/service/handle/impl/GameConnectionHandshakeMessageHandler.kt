package org.redrune.network.codec.service.handle.impl

import io.netty.channel.ChannelHandlerContext
import org.redrune.core.network.model.message.codec.impl.RS2MessageDecoder
import org.redrune.core.network.model.message.codec.impl.RawMessageEncoder
import org.redrune.core.network.model.packet.codec.impl.SimplePacketDecoder
import org.redrune.core.tools.utility.replace
import org.redrune.network.codec.login.LoginCodec
import org.redrune.network.codec.login.encode.message.LoginConnectionResponseMessage
import org.redrune.network.codec.service.decode.message.GameConnectionHandshakeMessage
import org.redrune.network.codec.service.handle.ServiceMessageHandler
import org.redrune.network.NetworkChannelHandler

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class GameConnectionHandshakeMessageHandler : ServiceMessageHandler<GameConnectionHandshakeMessage>() {

    override fun handle(ctx: ChannelHandlerContext, msg: GameConnectionHandshakeMessage) {
        val pipeline = ctx.pipeline()
        pipeline.apply {
            replace("packet.decoder", SimplePacketDecoder(LoginCodec))
            replace("message.decoder", RS2MessageDecoder(LoginCodec))
            replace("message.handler", NetworkChannelHandler(LoginCodec))
            replace("message.encoder", RawMessageEncoder(LoginCodec))
        }

        ctx.pipeline().writeAndFlush(LoginConnectionResponseMessage(0))
    }

}