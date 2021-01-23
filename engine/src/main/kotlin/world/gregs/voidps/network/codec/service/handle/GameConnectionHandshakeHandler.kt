package world.gregs.voidps.network.codec.service.handle

import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.network.codec.Handler
import world.gregs.voidps.network.codec.login.LoginCodec
import world.gregs.voidps.network.codec.login.encode.LoginResponseEncoder
import world.gregs.voidps.network.codec.setCodec
import world.gregs.voidps.utility.inject

class GameConnectionHandshakeHandler : Handler() {

	private val login: LoginCodec by inject()
	private val responseEncoder = LoginResponseEncoder()

	override fun gameHandshake(context: ChannelHandlerContext) {
		val channel = context.channel()
		channel.setCodec(login)
		responseEncoder.encode(channel, 0)
	}
	
}