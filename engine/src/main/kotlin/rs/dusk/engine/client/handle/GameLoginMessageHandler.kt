package rs.dusk.engine.client.handle

import io.netty.channel.ChannelHandlerContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import rs.dusk.core.network.codec.message.decode.OpcodeMessageDecoder
import rs.dusk.core.network.codec.message.encode.GenericMessageEncoder
import rs.dusk.core.network.codec.message.handle.NetworkMessageHandler
import rs.dusk.core.network.codec.packet.access.PacketBuilder
import rs.dusk.core.network.codec.packet.decode.RS2PacketDecoder
import rs.dusk.core.network.model.session.getSession
import rs.dusk.core.tools.utility.replace
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.entity.factory.PlayerFactory
import rs.dusk.network.rs.ServerNetworkEventHandler
import rs.dusk.network.rs.codec.game.GameCodec
import rs.dusk.network.rs.codec.login.LoginCodec
import rs.dusk.network.rs.codec.login.LoginMessageHandler
import rs.dusk.network.rs.codec.login.decode.message.GameLoginMessage
import rs.dusk.network.rs.codec.login.encode.message.GameLoginDetails
import rs.dusk.network.rs.session.GameSession
import rs.dusk.utility.crypto.cipher.IsaacKeyPair
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
class GameLoginMessageHandler : LoginMessageHandler<GameLoginMessage>() {

    val sessions: Sessions by inject()
    val factory: PlayerFactory by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: GameLoginMessage) {
        val pipeline = ctx.pipeline()
        val keyPair = IsaacKeyPair(msg.isaacKeys)
        pipeline.replace("message.encoder", GenericMessageEncoder(LoginCodec, PacketBuilder(sized = true)))

        pipeline.writeAndFlush(GameLoginDetails())

        with(pipeline) {
            replace(
                "packet.decoder", RS2PacketDecoder(
                    keyPair.inCipher,
                    GameCodec
                )
            )
            replace("message.decoder", OpcodeMessageDecoder(GameCodec))
            replace(
                "message.handler", NetworkMessageHandler(
                    GameCodec,
                    ServerNetworkEventHandler(GameSession(channel()))
                )
            )
            replace("message.encoder", GenericMessageEncoder(GameCodec, PacketBuilder(keyPair.outCipher)))
        }

        GlobalScope.launch {
            val session = ctx.channel().getSession()
            factory.spawn(msg.username, session).await()
            sessions.send(session, msg)
        }
    }

}