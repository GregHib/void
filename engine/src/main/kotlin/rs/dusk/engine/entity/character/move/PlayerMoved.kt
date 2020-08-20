package rs.dusk.engine.entity.character.move

import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerEvent
import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.map.Tile

data class PlayerMoved(override val player: Player, val from: Tile, val to: Tile) : PlayerEvent() {
    companion object : EventCompanion<PlayerMoved>
}