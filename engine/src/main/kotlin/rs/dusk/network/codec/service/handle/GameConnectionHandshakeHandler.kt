package rs.dusk.network.codec.service.handle

import io.netty.channel.ChannelHandlerContext
import rs.dusk.network.codec.Handler
import rs.dusk.network.codec.login.LoginCodec
import rs.dusk.network.codec.login.encode.LoginResponseEncoder
import rs.dusk.network.codec.setCodec
import rs.dusk.utility.inject

class GameConnectionHandshakeHandler : Handler() {

	private val login: LoginCodec by inject()
	private val responseEncoder = LoginResponseEncoder()

	override fun gameHandshake(context: ChannelHandlerContext) {
		println("Game hanshake")
		val channel = context.channel()
		channel.setCodec(login)
		responseEncoder.encode(channel, 0)
	}
	
}