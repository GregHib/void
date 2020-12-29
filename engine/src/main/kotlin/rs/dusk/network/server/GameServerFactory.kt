package rs.dusk.network.server

import org.koin.dsl.module
import rs.dusk.core.network.connection.ConnectionFactory
import rs.dusk.core.network.connection.ConnectionPipeline
import rs.dusk.core.network.connection.event.ChannelEventChain
import rs.dusk.core.network.connection.event.ChannelEventType.ACTIVE
import rs.dusk.core.network.connection.event.ChannelEventType.INACTIVE
import rs.dusk.core.network.connection.event.type.ChannelActiveEvent
import rs.dusk.core.network.connection.event.type.ChannelInactiveEvent
import rs.dusk.network.rs.codec.game.GameCodec
import rs.dusk.network.rs.codec.login.LoginCodec
import rs.dusk.network.rs.codec.service.ServiceCodec
import rs.dusk.network.rs.codec.update.UpdateCodec

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since May 27, 2020
 */
class GameServerFactory : ConnectionFactory() {

    fun bind(server: GameServer, chain: ChannelEventChain, pipeline: ConnectionPipeline) = with(chain) {
        append(ACTIVE, ChannelActiveEvent(server, channels))
        append(INACTIVE, ChannelInactiveEvent(server, channels))

        server.configure(pipeline)
        server.bind()
    }
}

val gameServerFactory = module {
    single { GameServerFactory() }
    single(createdAtStart = true) { GameCodec().apply { run() } }
    single(createdAtStart = true) { LoginCodec().apply { run() } }
    single(createdAtStart = true) { ServiceCodec().apply { run() } }
    single(createdAtStart = true) { UpdateCodec().apply { run() } }
}