package rs.dusk.network.rs.codec.login.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.decode.OpcodeMessageDecoder
import rs.dusk.core.network.codec.message.encode.GenericMessageEncoder
import rs.dusk.core.network.codec.message.handle.NetworkMessageHandler
import rs.dusk.core.network.codec.packet.access.PacketBuilder
import rs.dusk.core.network.codec.packet.decode.RS2PacketDecoder
import rs.dusk.core.network.model.session.getSession
import rs.dusk.core.tools.utility.replace
import rs.dusk.network.rs.ServerNetworkEventHandler
import rs.dusk.network.rs.codec.game.GameCodec
import rs.dusk.network.rs.codec.login.LoginCodec
import rs.dusk.network.rs.codec.login.LoginMessageHandler
import rs.dusk.network.rs.codec.login.decode.message.LobbyLoginMessage
import rs.dusk.network.rs.codec.login.encode.message.LobbyConfigurationMessage
import rs.dusk.network.rs.session.GameSession
import rs.dusk.utility.crypto.cipher.IsaacKeyPair

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class LobbyLoginMessageHandler : LoginMessageHandler<LobbyLoginMessage>() {

    override fun handle(ctx: ChannelHandlerContext, msg: LobbyLoginMessage) {
        val pipeline = ctx.pipeline()
        val keyPair = IsaacKeyPair(msg.isaacSeed)
        pipeline.replace("message.encoder", GenericMessageEncoder(LoginCodec, PacketBuilder(sized = true)))

        println("issac seed = ${msg.isaacSeed.contentToString()}")

        pipeline.writeAndFlush(
            LobbyConfigurationMessage(
                msg.username,
                ctx.channel().getSession().getIp(),
                System.currentTimeMillis()
            )
        )

        with(pipeline) {
            replace("packet.decoder", RS2PacketDecoder(keyPair.inCipher, GameCodec))
            replace("message.decoder", OpcodeMessageDecoder(GameCodec))
            replace(
                "message.handler", NetworkMessageHandler(
                    GameCodec,
                    ServerNetworkEventHandler(GameSession(channel()))
                )
            )
            replace("message.encoder", GenericMessageEncoder(GameCodec, PacketBuilder(keyPair.outCipher)))
        }
    }
}