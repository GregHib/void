package rs.dusk.network.server

import org.koin.dsl.module
import rs.dusk.core.network.connection.ConnectionFactory
import rs.dusk.core.network.connection.ConnectionPipeline
import rs.dusk.core.network.connection.event.ChannelEventChain
import rs.dusk.core.network.connection.event.ChannelEventType.ACTIVE
import rs.dusk.core.network.connection.event.ChannelEventType.INACTIVE
import rs.dusk.core.network.connection.event.type.ChannelActiveEvent
import rs.dusk.core.network.connection.event.type.ChannelInactiveEvent

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since May 27, 2020
 */
class GameServerFactory : ConnectionFactory() {
	
	fun bind(server : GameServer, chain : ChannelEventChain, pipeline : ConnectionPipeline) = with(chain) {
		append(ACTIVE, ChannelActiveEvent(server, channels))
		append(INACTIVE, ChannelInactiveEvent(server, channels))
		
		server.configure(pipeline)
		server.bind()
	}
}

val gameServerFactory = module {
	single { GameServerFactory() }
}