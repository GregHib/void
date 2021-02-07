package world.gregs.voidps.engine.entity.character.player.login

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerEvent
import world.gregs.voidps.engine.event.EventCompanion

data class PlayerRegistered(override val player: Player) : PlayerEvent() {
    companion object : EventCompanion<PlayerRegistered>
}