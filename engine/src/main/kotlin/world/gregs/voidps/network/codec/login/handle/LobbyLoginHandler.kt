package world.gregs.voidps.network.codec.login.handle

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import world.gregs.voidps.network.codec.Handler
import world.gregs.voidps.network.codec.game.GameCodec
import world.gregs.voidps.network.codec.login.LoginCodec
import world.gregs.voidps.network.codec.login.encode.LobbyConfigurationEncoder
import world.gregs.voidps.network.codec.setCipherIn
import world.gregs.voidps.network.codec.setCipherOut
import world.gregs.voidps.network.codec.setCodec
import world.gregs.voidps.network.crypto.IsaacKeyPair
import world.gregs.voidps.utility.inject
import java.net.InetSocketAddress

class LobbyLoginHandler : Handler() {

	private val login: LoginCodec by inject()
	private val game: GameCodec by inject()
	private val configEncoder = LobbyConfigurationEncoder()

	override fun loginLobby(context: ChannelHandlerContext, username: String, password: String, hd: Boolean, resize: Boolean, settings: String, affiliate: Int, isaacSeed: IntArray, crcMap: MutableMap<Int, Pair<Int, Int>>) {
		val keyPair = IsaacKeyPair(isaacSeed)
		val channel = context.channel()
		
		channel.setCodec(login)

		configEncoder.encode(
			channel,
			username,
			getIp(context.channel()),
			System.currentTimeMillis()
		)
		channel.setCodec(game)
		channel.setCipherIn(keyPair.inCipher)
		channel.setCipherOut(keyPair.outCipher)
	}

	/**
	 * The ip address of the channel that connected to the server
	 */
	private fun getIp(channel: Channel) : String {
		return (channel.localAddress() as? InetSocketAddress)?.address?.hostAddress ?: "127.0.0.1"
	}
}