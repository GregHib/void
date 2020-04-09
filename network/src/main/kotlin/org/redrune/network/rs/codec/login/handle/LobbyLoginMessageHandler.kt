package org.redrune.network.rs.codec.login.handle

import io.netty.channel.ChannelHandlerContext
import org.redrune.core.network.codec.message.decode.OpcodeMessageDecoder
import org.redrune.core.network.codec.message.encode.RS2MessageEncoder
import org.redrune.core.network.codec.message.encode.SizedMessageEncoder
import org.redrune.core.network.codec.message.handle.NetworkMessageHandler
import org.redrune.core.network.codec.packet.decode.RS2PacketDecoder
import org.redrune.core.network.model.session.getSession
import org.redrune.core.tools.utility.replace
import org.redrune.network.NetworkEventHandler
import org.redrune.network.rs.codec.game.GameCodec
import org.redrune.network.rs.codec.login.LoginCodec
import org.redrune.network.rs.codec.login.decode.message.LobbyLoginMessage
import org.redrune.network.rs.codec.login.encode.message.LobbyConfigurationMessage
import org.redrune.network.rs.codec.login.LoginMessageHandler
import org.redrune.utility.crypto.cipher.IsaacKeyPair

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class LobbyLoginMessageHandler : LoginMessageHandler<LobbyLoginMessage>() {

    override fun handle(ctx: ChannelHandlerContext, msg: LobbyLoginMessage) {
        val pipeline = ctx.pipeline()
        val keyPair = IsaacKeyPair(msg.isaacSeed)
        pipeline.replace("message.encoder", SizedMessageEncoder(LoginCodec))

        println("issac seed = ${msg.isaacSeed.contentToString()}")

        pipeline.writeAndFlush(
            LobbyConfigurationMessage(
                msg.username,
                ctx.channel().getSession().getHost(),
                System.currentTimeMillis()
            )
        )

        with(pipeline) {
            replace("packet.decoder", RS2PacketDecoder(GameCodec, keyPair.inCipher))
            replace("message.decoder", OpcodeMessageDecoder(GameCodec))
            replace("message.handler", NetworkMessageHandler(GameCodec,
                NetworkEventHandler()
            ))
            replace("message.encoder", RS2MessageEncoder(GameCodec, keyPair.outCipher))
        }
    }
}