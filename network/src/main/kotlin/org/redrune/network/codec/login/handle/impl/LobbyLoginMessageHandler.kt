package org.redrune.network.codec.login.handle.impl

import io.netty.channel.ChannelHandlerContext
import org.redrune.core.network.message.codec.impl.RS2MessageDecoder
import org.redrune.core.network.message.codec.impl.RS2MessageEncoder
import org.redrune.core.network.message.codec.impl.SizedMessageEncoder
import org.redrune.core.network.packet.codec.impl.RS2PacketDecoder
import org.redrune.core.network.session.getSession
import org.redrune.core.tools.utility.replace
import org.redrune.network.NetworkChannelHandler
import org.redrune.network.codec.game.GameCodec
import org.redrune.network.codec.login.LoginCodec
import org.redrune.network.codec.login.decode.message.LobbyLoginMessage
import org.redrune.network.codec.login.encode.message.LobbyConfigurationMessage
import org.redrune.network.codec.login.handle.LoginMessageHandler
import org.redrune.tools.crypto.cipher.IsaacKeyPair

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
            replace("message.decoder", RS2MessageDecoder(GameCodec))
            replace("message.handler", NetworkChannelHandler(GameCodec))
            replace("message.encoder", RS2MessageEncoder(GameCodec, keyPair.outCipher))
        }
    }
}