package rs.dusk.network.rs.codec.login.handle

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import rs.dusk.core.crypto.IsaacKeyPair
import rs.dusk.core.network.codec.message.MessageHandler
import rs.dusk.core.network.codec.setCipherIn
import rs.dusk.core.network.codec.setCipherOut
import rs.dusk.core.network.codec.setCodec
import rs.dusk.network.rs.codec.game.GameCodec
import rs.dusk.network.rs.codec.login.LoginCodec
import rs.dusk.network.rs.codec.login.encode.LobbyConfigurationMessageEncoder
import rs.dusk.utility.inject
import java.net.InetSocketAddress

class LobbyLoginMessageHandler : MessageHandler() {

	private val login: LoginCodec by inject()
	private val game: GameCodec by inject()
	private val configEncoder = LobbyConfigurationMessageEncoder()

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