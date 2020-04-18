package rs.dusk.engine.entity.factory

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.sync.Mutex
import rs.dusk.core.network.model.session.Session
import rs.dusk.engine.client.ClientSessions
import rs.dusk.engine.client.IndexAllocator
import rs.dusk.engine.data.PlayerLoader
import rs.dusk.engine.entity.event.Registered
import rs.dusk.engine.event.EventBus
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
class PlayerFactory {

    private val loader: PlayerLoader by inject()
    private val bus: EventBus by inject()
    private val indexer: IndexAllocator by inject()
    private val sessions: ClientSessions by inject()
    private val mutex = Mutex()

    fun spawn(name: String, session: Session? = null) = GlobalScope.async {
        val player = loader.load(name)
        mutex.withLock {
            val index = indexer.obtain()
            if (index != null) {
                player.index = index
            } else {
                return@async null
            }
        }
        if (session != null) {
            sessions.register(session, player)
        }
        bus.emit(Registered(player))
        player
    }
}