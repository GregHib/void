package world.gregs.void.network.codec.service.handle

import io.netty.channel.ChannelHandlerContext
import world.gregs.void.network.codec.Handler
import world.gregs.void.network.codec.login.LoginCodec
import world.gregs.void.network.codec.login.encode.LoginResponseEncoder
import world.gregs.void.network.codec.setCodec
import world.gregs.void.utility.inject

class GameConnectionHandshakeHandler : Handler() {

	private val login: LoginCodec by inject()
	private val responseEncoder = LoginResponseEncoder()

	override fun gameHandshake(context: ChannelHandlerContext) {
		val channel = context.channel()
		channel.setCodec(login)
		responseEncoder.encode(channel, 0)
	}
	
}