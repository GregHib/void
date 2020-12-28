package rs.dusk.network.rs.codec.login.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.io.crypto.IsaacKeyPair
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.codec.message.decode.OpcodeMessageDecoder
import rs.dusk.core.network.codec.message.encode.GenericMessageEncoder
import rs.dusk.core.network.codec.packet.access.PacketBuilder
import rs.dusk.core.network.codec.packet.decode.RS2PacketDecoder
import rs.dusk.core.network.codec.setCodec
import rs.dusk.core.network.model.session.getSession
import rs.dusk.core.utility.replace
import rs.dusk.network.rs.codec.game.GameCodec
import rs.dusk.network.rs.codec.login.LoginCodec
import rs.dusk.network.rs.codec.login.encode.message.LobbyConfigurationMessage

class LobbyLoginMessageHandler : MessageHandler() {

	override fun loginLobby(context: ChannelHandlerContext, username: String, password: String, hd: Boolean, resize: Boolean, settings: String, affiliate: Int, isaacSeed: IntArray, crcMap: MutableMap<Int, Pair<Int, Int>>) {
		val pipeline = context.pipeline()
		val keyPair = IsaacKeyPair(isaacSeed)
		val channel = context.channel()
		
		channel.setCodec(LoginCodec)
		pipeline.replace("message.encoder", GenericMessageEncoder(PacketBuilder(sized = true)))
		
		println("issac seed = ${isaacSeed.contentToString()}")
		
		pipeline.writeAndFlush(
			LobbyConfigurationMessage(
				username,
				context.channel().getSession().getIp(),
				System.currentTimeMillis()
			)
		)
		channel.setCodec(GameCodec)
		
		with(pipeline) {
			replace("packet.decoder", RS2PacketDecoder(keyPair.inCipher))
			replace("message.decoder", OpcodeMessageDecoder())
			replace("message.encoder", GenericMessageEncoder(PacketBuilder(keyPair.outCipher)))
		}
	}
}