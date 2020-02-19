package org.redrune.network.codec.login.handle.impl

import io.netty.channel.ChannelHandlerContext
import org.redrune.network.codec.game.GameCodec
import org.redrune.network.codec.login.decode.message.LoginServiceLobbyMessage
import org.redrune.network.codec.login.encode.message.LobbyConfigurationMessage
import org.redrune.network.getSession
import org.redrune.network.message.codec.MessageHandler
import org.redrune.network.message.codec.game.GameMessageEncoder
import org.redrune.network.packet.codec.impl.GamePacketDecoder
import org.redrune.tools.crypto.cipher.IsaacCipher
import java.util.*

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class LoginServiceLobbyMessageHandler : MessageHandler<LoginServiceLobbyMessage>() {

    override fun handle(ctx: ChannelHandlerContext, msg: LoginServiceLobbyMessage) {

        val isaacKeys = msg.isaacKey
        val inCipher: IntArray = Arrays.copyOf(isaacKeys, isaacKeys.size)
        val outCipher = IntArray(4)
        for (i in isaacKeys.indices) {
            outCipher[i] = isaacKeys.get(i) + 50
        }

        ctx.pipeline().replace("packet.decoder", "packet.decoder", GamePacketDecoder(GameCodec, IsaacCipher(outCipher)))
        ctx.pipeline()
            .replace("message.encoder", "message.encoder", GameMessageEncoder(GameCodec, IsaacCipher(outCipher)))

        ctx.pipeline().writeAndFlush(LobbyConfigurationMessage(msg.username, ctx.channel().getSession().getHost()))
    }
}