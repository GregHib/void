package rs.dusk.engine.model.entity.factory

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import rs.dusk.core.network.model.session.Session
import rs.dusk.engine.client.session.Sessions
import rs.dusk.engine.data.PlayerLoader
import rs.dusk.engine.model.entity.index.IndexAllocator
import rs.dusk.engine.model.entity.index.update.visual.player.name
import rs.dusk.engine.model.entity.list.MAX_PLAYERS
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.path.TraversalType
import rs.dusk.engine.path.traverse.SmallTraversal
import rs.dusk.utility.get
import rs.dusk.utility.inject

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
class PlayerFactory {

    private val logger = InlineLogger()
    private val loader: PlayerLoader by inject()
    private val indexer = IndexAllocator(MAX_PLAYERS)
    private val sessions: Sessions by inject()
    private val mutex = Mutex()
    private val scope = CoroutineScope(newSingleThreadContext("PlayerFactory"))
    private val small = SmallTraversal(TraversalType.Land, false, get())

    fun spawn(name: String, tile: Tile? = null, session: Session? = null) = scope.async {
        val player = if (tile != null) loader.loadPlayer(name, tile) else loader.loadPlayer(name = name)
        player.movement.traversal = small
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
        player
    }
}