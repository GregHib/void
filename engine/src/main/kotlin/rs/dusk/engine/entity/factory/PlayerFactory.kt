package rs.dusk.engine.entity.factory

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.redrune.core.network.model.session.Session
import org.redrune.engine.client.ClientSessions
import org.redrune.engine.client.IndexAllocator
import org.redrune.engine.data.PlayerLoader
import org.redrune.engine.entity.event.Registered
import org.redrune.engine.event.EventBus
import org.redrune.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
class PlayerFactory {

    private val logger = InlineLogger()
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