package rs.dusk.network.rs.codec.service.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.codec.message.decode.OpcodeMessageDecoder
import rs.dusk.core.network.codec.message.encode.GenericMessageEncoder
import rs.dusk.core.network.codec.packet.decode.SimplePacketDecoder
import rs.dusk.core.network.codec.setCodec
import rs.dusk.core.utility.replace
import rs.dusk.network.rs.codec.login.LoginCodec
import rs.dusk.network.rs.codec.login.encode.message.LobbyLoginConnectionResponseMessage

class GameConnectionHandshakeMessageHandler : MessageHandler() {

	override fun gameHandshake(context: ChannelHandlerContext) {
		val pipeline = context.pipeline()
		val channel = context.channel()
		
		channel.setCodec(LoginCodec)
		
		pipeline.apply {
			replace("packet.decoder", SimplePacketDecoder())
			replace("message.decoder", OpcodeMessageDecoder())
			replace("message.encoder", GenericMessageEncoder())
		}
		context.pipeline().writeAndFlush(LobbyLoginConnectionResponseMessage(0))
	}
	
}