package world.gregs.voidps.world.interact.entity.player.spawn

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.Sessions
import world.gregs.voidps.engine.data.StorageStrategy
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerSpawn
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.engine.path.TraversalType
import world.gregs.voidps.engine.path.traverse.SmallTraversal
import world.gregs.voidps.engine.delay
import world.gregs.voidps.utility.get
import world.gregs.voidps.utility.inject

val players: Players by inject()

val logger = InlineLogger()
val sessions: Sessions by inject()
val small = SmallTraversal(TraversalType.Land, false, get())
val storage: StorageStrategy<Player> by inject()

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
    players.remove(player.tile, player)
    players.remove(player.tile.chunk, player)
    delay(1) {
        players.removeAtIndex(player.index)
    }
    val session = sessions.get(player)
    if (session != null) {
        session.disconnect()
        sessions.deregister(session)
    }
    storage.save(player.name, player)
}