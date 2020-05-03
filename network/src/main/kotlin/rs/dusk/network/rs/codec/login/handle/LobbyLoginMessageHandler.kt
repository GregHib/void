package rs.dusk.network.rs.codec.login.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.CodecRepository
import rs.dusk.core.network.codec.message.MessageReader
import rs.dusk.core.network.codec.message.decode.OpcodeMessageDecoder
import rs.dusk.core.network.codec.message.encode.GenericMessageEncoder
import rs.dusk.core.network.codec.packet.access.PacketBuilder
import rs.dusk.core.network.codec.packet.decode.RS2PacketDecoder
import rs.dusk.core.network.connection.event.ConnectionEventListener
import rs.dusk.core.network.model.session.getSession
import rs.dusk.core.utility.replace
import rs.dusk.network.rs.ServerConnectionEventChain
import rs.dusk.network.rs.codec.game.GameCodec
import rs.dusk.network.rs.codec.login.LoginCodec
import rs.dusk.network.rs.codec.login.LoginMessageHandler
import rs.dusk.network.rs.codec.login.decode.message.LobbyLoginMessage
import rs.dusk.network.rs.codec.login.encode.message.LobbyConfigurationMessage
import rs.dusk.network.rs.session.GameSession
import rs.dusk.utility.crypto.cipher.IsaacKeyPair
import rs.dusk.utility.inject

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class LobbyLoginMessageHandler : LoginMessageHandler<LobbyLoginMessage>() {

	val repository: CodecRepository by inject()

	override fun handle(ctx: ChannelHandlerContext, msg: LobbyLoginMessage) {
		val pipeline = ctx.pipeline()
		val keyPair = IsaacKeyPair(msg.isaacSeed)
		val gameCodec = repository.get(GameCodec::class)
		val loginCodec = repository.get(LoginCodec::class)
		pipeline.replace("message.encoder", GenericMessageEncoder(loginCodec, PacketBuilder(sized = true)))

		println("issac seed = ${msg.isaacSeed.contentToString()}")

		pipeline.writeAndFlush(
			LobbyConfigurationMessage(
				msg.username,
				ctx.channel().getSession().getIp(),
				System.currentTimeMillis()
			)
		)
		val codec = repository.get(GameCodec::class)

		with(pipeline) {
			val session = GameSession(channel())
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
			replace("message.encoder", GenericMessageEncoder(codec, PacketBuilder(keyPair.outCipher)))
			replace("connection.listener", ConnectionEventListener(ServerConnectionEventChain(session)))
		}
	}
}