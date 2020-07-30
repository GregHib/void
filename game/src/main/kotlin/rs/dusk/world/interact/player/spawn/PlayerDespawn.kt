package rs.dusk.world.interact.player.spawn

import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerEvent
import rs.dusk.engine.event.EventCompanion

data class PlayerDespawn(override val player: Player) : PlayerEvent() {
    companion object : EventCompanion<PlayerDespawn>
}