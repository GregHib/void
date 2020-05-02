package rs.dusk.engine.client.handle

import com.github.michaelbull.logging.InlineLogger
import io.netty.channel.ChannelHandlerContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import rs.dusk.core.network.codec.CodecRepository
import rs.dusk.core.network.codec.message.MessageReader
import rs.dusk.core.network.codec.message.decode.OpcodeMessageDecoder
import rs.dusk.core.network.codec.message.encode.GenericMessageEncoder
import rs.dusk.core.network.codec.packet.access.PacketBuilder
import rs.dusk.core.network.codec.packet.decode.RS2PacketDecoder
import rs.dusk.core.network.connection.event.ConnectionEventListener
import rs.dusk.core.network.model.session.getSession
import rs.dusk.core.utility.replace
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.entity.factory.PlayerFactory
import rs.dusk.engine.entity.model.visual.visuals.player.name
import rs.dusk.network.rs.ServerConnectionEventChain
import rs.dusk.network.rs.codec.game.GameCodec
import rs.dusk.network.rs.codec.login.LoginCodec
import rs.dusk.network.rs.codec.login.LoginMessageHandler
import rs.dusk.network.rs.codec.login.decode.message.GameLoginMessage
import rs.dusk.network.rs.codec.login.encode.message.GameLoginDetails
import rs.dusk.utility.crypto.cipher.IsaacKeyPair
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
class GameLoginMessageHandler : LoginMessageHandler<GameLoginMessage>() {

	val logger = InlineLogger()
	val sessions: Sessions by inject()
	val factory: PlayerFactory by inject()
	val repository: CodecRepository by inject()

	override fun handle(ctx: ChannelHandlerContext, msg: GameLoginMessage) {
		val pipeline = ctx.pipeline()
		val keyPair = IsaacKeyPair(msg.isaacKeys)
		val gameCodec = repository.get(GameCodec::class)
		pipeline.replace(
			"message.encoder",
			GenericMessageEncoder(repository.get(LoginCodec::class), PacketBuilder(sized = true))
		)

		GlobalScope.launch {
			val session = ctx.channel().getSession()
			val player = factory.spawn(msg.username, session).await()
				?: return@launch logger.warn { "Unable to load player '${msg.username}'." }

			pipeline.writeAndFlush(GameLoginDetails(2, player.index, player.name))

			with(pipeline) {
				replace(
					"packet.decoder", RS2PacketDecoder(
						keyPair.inCipher,
						gameCodec
					)
				)
				replace("message.decoder", OpcodeMessageDecoder(gameCodec))
				replace(
					"message.reader", MessageReader(
						gameCodec
					)
				)
				replace("message.encoder", GenericMessageEncoder(gameCodec, PacketBuilder(keyPair.outCipher)))
				replace("connection.listener", ConnectionEventListener(ServerConnectionEventChain(session)))
			}

			sessions.send(session, msg)
		}
	}

}