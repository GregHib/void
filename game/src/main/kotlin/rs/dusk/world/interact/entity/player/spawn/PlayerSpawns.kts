package rs.dusk.world.interact.entity.player.spawn

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.entity.character.player.PlayerSpawn
import rs.dusk.engine.entity.character.player.Players
import rs.dusk.engine.entity.character.update.visual.player.name
import rs.dusk.engine.event.then
import rs.dusk.engine.path.TraversalType
import rs.dusk.engine.path.traverse.SmallTraversal
import rs.dusk.utility.get
import rs.dusk.utility.inject

val players: Players by inject()

val logger = InlineLogger()
val sessions: Sessions by inject()
val small = SmallTraversal(TraversalType.Land, false, get())

PlayerSpawn then {
    player.movement.traversal = small
    val session = session
    if (session != null) {
        sessions.register(session, player)
    }
    player.name = name
    data?.let { data ->
        player.gameFrame.width = data.width
        player.gameFrame.height = data.height
        player.gameFrame.displayMode = data.mode
    }
    logger.info { "Player spawned $name index ${player.index}." }
    result = player
}

PlayerDespawn then {
    players.remove(player)
    val session = sessions.get(player)
    if (session != null) {
        session.disconnect()
        sessions.deregister(session)
    }
}