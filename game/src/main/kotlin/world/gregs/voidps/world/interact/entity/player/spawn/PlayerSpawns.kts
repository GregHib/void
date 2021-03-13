package world.gregs.voidps.world.interact.entity.player.spawn

import world.gregs.voidps.engine.client.Sessions
import world.gregs.voidps.engine.data.StorageStrategy
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.logout.PlayerUnregistered
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.event.then
import world.gregs.voidps.utility.inject

val players: Players by inject()

val sessions: Sessions by inject()
val storage: StorageStrategy<Player> by inject()

PlayerUnregistered then {
    players.remove(player.tile, player)
    players.remove(player.tile.chunk, player)
    delay(1) {
        players.removeAtIndex(player.index)
    }
    val session = sessions.get(player)
    if (session != null) {
        session.disconnect()
        sessions.deregister(player)
    }
    storage.save(player.name, player)
}