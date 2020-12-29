package rs.dusk.network.server

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.core.network.NetworkServer
import rs.dusk.core.network.codec.message.decode.OpcodeMessageDecoder
import rs.dusk.core.network.codec.message.encode.GenericMessageEncoder
import rs.dusk.core.network.codec.packet.decode.SimplePacketDecoder
import rs.dusk.core.network.codec.setCodec
import rs.dusk.core.network.connection.ConnectionPipeline
import rs.dusk.core.network.connection.Session
import rs.dusk.core.network.connection.event.ChannelEvent
import rs.dusk.core.network.connection.event.ChannelEventChain
import rs.dusk.core.network.connection.event.ChannelEventListener
import rs.dusk.core.network.connection.event.ChannelEventType.DEREGISTER
import rs.dusk.core.network.connection.event.ChannelEventType.EXCEPTION
import rs.dusk.core.network.connection.event.type.ChannelExceptionEvent
import rs.dusk.core.network.connection.setSession
import rs.dusk.network.rs.codec.service.ServiceCodec
import rs.dusk.utility.get
import rs.dusk.utility.inject

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since April 13, 2020
 */
class GameServer(
	override val port: Int,
	private val disconnectEvent: ChannelEvent
) : NetworkServer() {
	
	private val logger = InlineLogger()
	
	override fun onConnect() {
		logger.info { "Connected!" }
	}
	
	override fun onDisconnect() {
		logger.info { "Disconnected!" }
	}
	
	/**
	 * If the game server is running
	 */
	var running = false

	fun run() {
		val factory : GameServerFactory by inject()

		val chain = ChannelEventChain().apply {
			append(EXCEPTION, ChannelExceptionEvent())
			append(DEREGISTER, disconnectEvent)
		}
		val service: ServiceCodec = get()
		val pipeline = ConnectionPipeline {
			val channel = it.channel()
			
			it.addLast("packet.decoder", SimplePacketDecoder())
			it.addLast("message.decoder", OpcodeMessageDecoder)
			it.addLast("message.encoder", GenericMessageEncoder)
			it.addLast("channel.listener", ChannelEventListener(chain))

			channel.setCodec(service)
			channel.setSession(Session(channel))
		}
		factory.bind(this, chain, pipeline)
		running = true
	}
}