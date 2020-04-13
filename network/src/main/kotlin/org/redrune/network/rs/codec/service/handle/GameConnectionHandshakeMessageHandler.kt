package org.redrune.network.rs.codec.service.handle

import io.netty.channel.ChannelHandlerContext
import org.redrune.core.network.codec.message.decode.OpcodeMessageDecoder
import org.redrune.core.network.codec.message.encode.GenericMessageEncoder
import org.redrune.core.network.codec.message.handle.NetworkMessageHandler
import org.redrune.core.network.codec.packet.decode.SimplePacketDecoder
import org.redrune.core.tools.utility.replace
import org.redrune.network.rs.ServerNetworkEventHandler
import org.redrune.network.rs.codec.login.LoginCodec
import org.redrune.network.rs.codec.login.encode.message.LoginConnectionResponseMessage
import org.redrune.network.rs.codec.service.ServiceMessageHandler
import org.redrune.network.rs.codec.service.decode.message.GameConnectionHandshakeMessage
import org.redrune.network.rs.session.LoginSession

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class GameConnectionHandshakeMessageHandler : ServiceMessageHandler<GameConnectionHandshakeMessage>() {

    override fun handle(ctx: ChannelHandlerContext, msg: GameConnectionHandshakeMessage) {
        val pipeline = ctx.pipeline()
        pipeline.apply {
            replace("packet.decoder", SimplePacketDecoder(LoginCodec))
            replace("message.decoder", OpcodeMessageDecoder(LoginCodec))
            replace(
                "message.handler", NetworkMessageHandler(
                    LoginCodec,
                    ServerNetworkEventHandler(LoginSession(channel()))
                )
            )
            replace("message.encoder", GenericMessageEncoder(LoginCodec))
        }
        ctx.pipeline().writeAndFlush(LoginConnectionResponseMessage(0))
    }

}