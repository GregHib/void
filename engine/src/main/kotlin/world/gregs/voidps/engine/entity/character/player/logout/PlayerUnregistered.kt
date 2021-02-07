package world.gregs.voidps.engine.entity.character.player.logout

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerEvent
import world.gregs.voidps.engine.event.EventCompanion

data class PlayerUnregistered(override val player: Player) : PlayerEvent() {
    companion object : EventCompanion<PlayerUnregistered>
}