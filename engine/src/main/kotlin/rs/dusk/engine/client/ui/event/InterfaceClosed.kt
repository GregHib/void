package rs.dusk.engine.client.ui.event

import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.engine.model.entity.character.player.PlayerEvent

data class InterfaceClosed(override val player: Player, val id: Int) : PlayerEvent() {
    companion object : EventCompanion<InterfaceClosed>
}
