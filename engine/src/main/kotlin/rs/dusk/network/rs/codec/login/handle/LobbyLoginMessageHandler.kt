package rs.dusk.network.rs.codec.login.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.crypto.IsaacKeyPair
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.codec.message.decode.OpcodeMessageDecoder
import rs.dusk.core.network.codec.message.encode.GenericMessageEncoder
import rs.dusk.core.network.codec.packet.decode.RS2PacketDecoder
import rs.dusk.core.network.codec.setCipherIn
import rs.dusk.core.network.codec.setCipherOut
import rs.dusk.core.network.codec.setCodec
import rs.dusk.core.network.codec.setSized
import rs.dusk.core.network.model.session.getSession
import rs.dusk.core.utility.replace
import rs.dusk.network.rs.codec.game.GameCodec
import rs.dusk.network.rs.codec.login.LoginCodec
import rs.dusk.network.rs.codec.login.encode.message.LobbyConfigurationMessage
import rs.dusk.utility.inject

class LobbyLoginMessageHandler : MessageHandler() {

	private val login: LoginCodec by inject()
	private val game: GameCodec by inject()

	override fun loginLobby(context: ChannelHandlerContext, username: String, password: String, hd: Boolean, resize: Boolean, settings: String, affiliate: Int, isaacSeed: IntArray, crcMap: MutableMap<Int, Pair<Int, Int>>) {
		val pipeline = context.pipeline()
		val keyPair = IsaacKeyPair(isaacSeed)
		val channel = context.channel()
		
		channel.setCodec(login)
		channel.setSized(true)
		pipeline.replace("message.encoder", GenericMessageEncoder)
		
		println("issac seed = ${isaacSeed.contentToString()}")
		
		pipeline.writeAndFlush(
			LobbyConfigurationMessage(
				username,
				context.channel().getSession().getIp(),
				System.currentTimeMillis()
			)
		)
		channel.setCodec(game)
		channel.setSized(false)
		channel.setCipherIn(keyPair.inCipher)
		channel.setCipherOut(keyPair.outCipher)
		with(pipeline) {
			replace("packet.decoder", RS2PacketDecoder())
			replace("message.decoder", OpcodeMessageDecoder)
			replace("message.encoder", GenericMessageEncoder)
		}
	}
}