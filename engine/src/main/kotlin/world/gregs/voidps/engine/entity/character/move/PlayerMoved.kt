package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerEvent
import world.gregs.voidps.engine.event.EventCompanion
import world.gregs.voidps.engine.map.Tile

data class PlayerMoved(override val player: Player, val from: Tile, val to: Tile) : PlayerEvent() {
    companion object : EventCompanion<PlayerMoved>
}