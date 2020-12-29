package rs.dusk.network.rs.codec.service.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.codec.setCodec
import rs.dusk.network.rs.codec.login.LoginCodec
import rs.dusk.network.rs.codec.login.encode.message.LobbyLoginConnectionResponseMessage
import rs.dusk.utility.inject

class GameConnectionHandshakeMessageHandler : MessageHandler() {

	private val login: LoginCodec by inject()

	override fun gameHandshake(context: ChannelHandlerContext) {
		val channel = context.channel()
		channel.setCodec(login)
		context.pipeline().writeAndFlush(LobbyLoginConnectionResponseMessage(0))
	}
	
}