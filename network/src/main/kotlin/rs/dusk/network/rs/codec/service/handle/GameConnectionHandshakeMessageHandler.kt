package rs.dusk.network.rs.codec.service.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.decode.OpcodeMessageDecoder
import rs.dusk.core.network.codec.message.encode.GenericMessageEncoder
import rs.dusk.core.network.codec.message.handle.NetworkMessageHandler
import rs.dusk.core.network.codec.packet.decode.SimplePacketDecoder
import rs.dusk.core.tools.utility.replace
import rs.dusk.network.rs.ServerNetworkEventHandler
import rs.dusk.network.rs.codec.login.LoginCodec
import rs.dusk.network.rs.codec.login.encode.message.LobbyLoginConnectionResponseMessage
import rs.dusk.network.rs.codec.service.ServiceMessageHandler
import rs.dusk.network.rs.codec.service.decode.message.GameConnectionHandshakeMessage
import rs.dusk.network.rs.session.LoginSession

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
        ctx.pipeline().writeAndFlush(LobbyLoginConnectionResponseMessage(0))
    }

}