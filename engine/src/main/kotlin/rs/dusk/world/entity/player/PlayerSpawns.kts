package rs.dusk.world.entity.player

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.engine.client.Sessions
import rs.dusk.engine.event.then
import rs.dusk.engine.model.entity.index.player.Players
import rs.dusk.engine.model.entity.index.update.visual.player.name
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
    if (session != null) {
        sessions.register(session, player)
    }
    player.name = name
    logger.info { "Player spawned $name index ${player.index}." }
    result = player
}

PlayerDespawn then {
    players.remove(player)
}