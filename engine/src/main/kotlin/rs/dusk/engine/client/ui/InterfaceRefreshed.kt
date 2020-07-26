package rs.dusk.engine.client.ui

import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.engine.model.entity.character.player.PlayerEvent

data class InterfaceRefreshed(override val player: Player, val id: Int) : PlayerEvent() {
    companion object : EventCompanion<InterfaceRefreshed>
}
