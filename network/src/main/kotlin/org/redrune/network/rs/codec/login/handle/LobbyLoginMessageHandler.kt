package org.redrune.network.rs.codec.login.handle

import io.netty.channel.ChannelHandlerContext
import org.redrune.core.network.codec.message.decode.OpcodeMessageDecoder
import org.redrune.core.network.codec.message.encode.GenericMessageEncoder
import org.redrune.core.network.codec.message.handle.NetworkMessageHandler
import org.redrune.core.network.codec.packet.access.PacketBuilder
import org.redrune.core.network.codec.packet.decode.RS2PacketDecoder
import org.redrune.core.network.model.session.getSession
import org.redrune.core.tools.utility.replace
import org.redrune.network.ServerNetworkEventHandler
import org.redrune.network.rs.codec.game.GameCodec
import org.redrune.network.rs.codec.login.LoginCodec
import org.redrune.network.rs.codec.login.LoginMessageHandler
import org.redrune.network.rs.codec.login.decode.message.LobbyLoginMessage
import org.redrune.network.rs.codec.login.encode.message.LobbyConfigurationMessage
import org.redrune.network.rs.session.GameSession
import org.redrune.utility.crypto.cipher.IsaacKeyPair

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