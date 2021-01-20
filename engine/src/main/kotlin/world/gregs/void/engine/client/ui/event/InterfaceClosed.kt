package world.gregs.void.engine.client.ui.event

import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.player.PlayerEvent
import world.gregs.void.engine.event.EventCompanion

data class InterfaceClosed(override val player: Player, val id: Int, val name: String) : PlayerEvent() {
    companion object : EventCompanion<InterfaceClosed>
}
