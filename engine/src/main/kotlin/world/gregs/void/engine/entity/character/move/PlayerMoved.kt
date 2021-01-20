package world.gregs.void.engine.entity.character.move

import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.player.PlayerEvent
import world.gregs.void.engine.event.EventCompanion
import world.gregs.void.engine.map.Tile

data class PlayerMoved(override val player: Player, val from: Tile, val to: Tile) : PlayerEvent() {
    companion object : EventCompanion<PlayerMoved>
}