package rs.dusk.network.rs.codec.login.handle

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
import rs.dusk.network.rs.codec.game.GameCodec
import rs.dusk.network.rs.codec.login.LoginCodec
import rs.dusk.network.rs.codec.login.LoginMessageHandler
import rs.dusk.network.rs.codec.login.decode.message.LobbyLoginMessage
import rs.dusk.network.rs.codec.login.encode.message.LobbyConfigurationMessage
import rs.dusk.utility.inject

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
class LobbyLoginMessageHandler : LoginMessageHandler<LobbyLoginMessage>() {
	
	private val repository : CodecRepository by inject()
	
	override fun handle(ctx : ChannelHandlerContext, msg : LobbyLoginMessage) {
		val pipeline = ctx.pipeline()
		val keyPair = IsaacKeyPair(msg.isaacSeed)
		val gameCodec = repository.get(GameCodec::class)
		val loginCodec = repository.get(LoginCodec::class)
		val channel = ctx.channel()
		
		channel.setCodec(loginCodec)
		pipeline.replace("message.encoder", GenericMessageEncoder(PacketBuilder(sized = true)))
		
		println("issac seed = ${msg.isaacSeed.contentToString()}")
		
		pipeline.writeAndFlush(
			LobbyConfigurationMessage(
				msg.username,
				ctx.channel().getSession().getIp(),
				System.currentTimeMillis()
			)
		)
		channel.setCodec(gameCodec)
		
		with(pipeline) {
			replace("packet.decoder", RS2PacketDecoder(keyPair.inCipher))
			replace("message.decoder", OpcodeMessageDecoder())
			replace("message.reader", MessageReader())
			replace("message.encoder", GenericMessageEncoder(PacketBuilder(keyPair.outCipher)))
		}
	}
}