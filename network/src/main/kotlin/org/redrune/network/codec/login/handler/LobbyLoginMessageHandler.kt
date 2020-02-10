package org.redrune.network.codec.login.handler

import io.netty.channel.ChannelHandlerContext
import org.redrune.network.codec.login.LoginCodec
import org.redrune.network.codec.login.message.LobbyConstructionMessage
import org.redrune.network.codec.login.message.LobbyLoginMessage
import org.redrune.network.model.message.MessageHandler
import org.redrune.network.model.message.OutboundGameMessageEncoder
import org.redrune.network.model.packet.GamePacketDecoder
import org.redrune.tools.crypto.IsaacRandom

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 10, 2020
 */
class LobbyLoginMessageHandler : MessageHandler<LobbyLoginMessage>() {
    override fun handle(ctx: ChannelHandlerContext, msg: LobbyLoginMessage) {
        /*if (true) {
            // TODO implement checks
            val response = LoginResponseCode.ACCOUNT_DISABLED
            ctx.pipeline().writeAndFlush(LoginResponseMessage(response))
            return
        }*/
        // if login will be successful, the issac ciphers must be used to encode the login message
        val pipeline = ctx.pipeline()
        val inKeys = msg.isaacKey
        val inCipher = IsaacRandom(inKeys)

        // the isaac keys used for the outgoing cipher
        val outKeys = IntArray(4)
        inKeys.forEachIndexed { index, i -> outKeys[index] = i + 50 }
        val outCipher = IsaacRandom(outKeys)


        pipeline.apply {
            replace("packet.decoder", "packet.decoder", GamePacketDecoder(LoginCodec, inCipher))
            replace("message.encode", "message.encode", OutboundGameMessageEncoder(LoginCodec, outCipher))
            writeAndFlush(LobbyConstructionMessage(msg.username, 2, "127.0.0.1"))
        }
    }
}
