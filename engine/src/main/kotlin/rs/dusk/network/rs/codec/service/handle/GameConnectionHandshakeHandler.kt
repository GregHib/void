package rs.dusk.network.rs.codec.service.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.network.codec.message.Handler
import rs.dusk.core.network.codec.setCodec
import rs.dusk.network.rs.codec.login.LoginCodec
import rs.dusk.network.rs.codec.login.encode.LoginResponseEncoder
import rs.dusk.utility.inject

class GameConnectionHandshakeHandler : Handler() {

	private val login: LoginCodec by inject()
	private val responseEncoder = LoginResponseEncoder()

	override fun gameHandshake(context: ChannelHandlerContext) {
		val channel = context.channel()
		channel.setCodec(login)
		responseEncoder.encode(channel, 0)
	}
	
}