package world.gregs.voidps.world.interact.entity.player.spawn

import world.gregs.voidps.engine.data.StorageStrategy
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.utility.inject

val players: Players by inject()
val storage: StorageStrategy<Player> by inject()

on<Unregistered> { player: Player ->
    players.remove(player.tile, player)
    players.remove(player.tile.chunk, player)
    delay(1) {
        players.removeAtIndex(player.index)
    }
    storage.save(player.name, player)
}