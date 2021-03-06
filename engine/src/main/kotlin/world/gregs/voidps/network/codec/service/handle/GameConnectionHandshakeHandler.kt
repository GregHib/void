package world.gregs.voidps.network.codec.service.handle

import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Handler
import world.gregs.voidps.network.codec.login.LoginCodec
import world.gregs.voidps.network.codec.login.encode.LoginResponseEncoder
import world.gregs.voidps.utility.inject

class GameConnectionHandshakeHandler : Handler() {

	private val login: LoginCodec by inject()
	private val responseEncoder = LoginResponseEncoder()

	override fun gameHandshake(session: ClientSession) {
//		channel.setCodec(login)
//		responseEncoder.encode(session, 0)
	}
	
}