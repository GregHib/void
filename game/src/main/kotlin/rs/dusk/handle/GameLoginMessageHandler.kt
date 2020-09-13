package rs.dusk.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.io.crypto.IsaacKeyPair
import rs.dusk.core.network.codec.CodecRepository
import rs.dusk.core.network.codec.message.MessageReader
import rs.dusk.core.network.codec.message.decode.OpcodeMessageDecoder
import rs.dusk.core.network.codec.message.encode.GenericMessageEncoder
import rs.dusk.core.network.codec.packet.access.PacketBuilder
import rs.dusk.core.network.codec.packet.decode.RS2PacketDecoder
import rs.dusk.core.network.codec.setCodec
import rs.dusk.core.network.model.session.getSession
import rs.dusk.core.utility.replace
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.entity.Registered
import rs.dusk.engine.entity.character.player.PlayerRegistered
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.map.region.RegionLogin
import rs.dusk.network.rs.codec.game.GameCodec
import rs.dusk.network.rs.codec.login.LoginCodec
import rs.dusk.network.rs.codec.login.LoginMessageHandler
import rs.dusk.network.rs.codec.login.decode.message.GameLoginMessage
import rs.dusk.network.rs.codec.login.encode.message.GameLoginConnectionResponseMessage
import rs.dusk.network.rs.codec.login.encode.message.GameLoginDetails
import rs.dusk.utility.inject
import rs.dusk.world.interact.entity.player.spawn.login.Login
import rs.dusk.world.interact.entity.player.spawn.login.LoginResponse

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
class GameLoginMessageHandler : LoginMessageHandler<GameLoginMessage>() {

    val logger = InlineLogger()
    val sessions: Sessions by inject()
    val bus: EventBus by inject()
    val repository: CodecRepository by inject()

    override fun handle(ctx: ChannelHandlerContext, msg: GameLoginMessage) {
        val channel = ctx.channel()
        val pipeline = ctx.pipeline()
        val keyPair = IsaacKeyPair(msg.isaacKeys)

        channel.setCodec(repository.get(LoginCodec::class))
        pipeline.replace("message.encoder", GenericMessageEncoder(PacketBuilder(sized = true)))

        val session = ctx.channel().getSession()

        val callback: (LoginResponse) -> Unit = { response ->
            if (response is LoginResponse.Success) {
                val player = response.player
                pipeline.writeAndFlush(GameLoginDetails(2, player.index, msg.username))

                with(pipeline) {
                    replace("packet.decoder", RS2PacketDecoder(keyPair.inCipher))
                    replace("message.decoder", OpcodeMessageDecoder())
                    replace("message.reader", MessageReader())
                    replace("message.encoder", GenericMessageEncoder(PacketBuilder(keyPair.outCipher)))
                }

                channel.setCodec(repository.get(GameCodec::class))

                bus.emit(RegionLogin(player))
                bus.emit(PlayerRegistered(player))
                player.start()
                bus.emit(Registered(player))
            } else {
                pipeline.writeAndFlush(GameLoginConnectionResponseMessage(response.code))
            }
        }

        bus.emit(
            Login(
                msg.username,
                session,
                callback,
                msg
            )
        )
    }
}