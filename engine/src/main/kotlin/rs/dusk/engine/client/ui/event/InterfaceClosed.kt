package rs.dusk.engine.client.ui.event

import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerEvent
import rs.dusk.engine.event.EventCompanion

data class InterfaceClosed(override val player: Player, val id: Int, val name: String) : PlayerEvent() {
    companion object : EventCompanion<InterfaceClosed>
}
