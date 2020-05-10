package rs.dusk.engine.entity.factory

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import rs.dusk.core.network.model.session.Session
import rs.dusk.engine.client.IndexAllocator
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.data.PlayerLoader
import rs.dusk.engine.entity.event.Registered
import rs.dusk.engine.entity.list.MAX_PLAYERS
import rs.dusk.engine.entity.model.visual.visuals.player.name
import rs.dusk.engine.event.EventBus
import rs.dusk.engine.model.Tile
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
class PlayerFactory {

    private val logger = InlineLogger()
    private val loader: PlayerLoader by inject()
    private val bus: EventBus by inject()
    private val indexer = IndexAllocator(MAX_PLAYERS)
    private val sessions: Sessions by inject()
    private val mutex = Mutex()

    fun spawn(name: String, tile: Tile? = null, session: Session? = null) = GlobalScope.async {
        val player = if (tile != null) loader.loadPlayer(name, tile) else loader.loadPlayer(name = name)
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
        player.name = name
        logger.info { "Player save loaded $name index ${player.index}." }
        bus.emit(Registered(player))
        player
    }
}