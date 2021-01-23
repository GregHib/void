package world.gregs.voidps.engine.client.ui.event

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerEvent
import world.gregs.voidps.engine.event.EventCompanion

data class InterfaceOpened(override val player: Player, val id: Int, val name: String) : PlayerEvent() {
    companion object : EventCompanion<InterfaceOpened>
}
