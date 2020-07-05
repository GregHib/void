package rs.dusk.world.entity.player

import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerEvent

data class PlayerDespawn(override val player: Player) : PlayerEvent() {
    companion object : EventCompanion<PlayerDespawn>
}