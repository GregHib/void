package world.gregs.void.world.interact.entity.player.spawn

import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.player.PlayerEvent
import world.gregs.void.engine.event.EventCompanion

data class PlayerDespawn(override val player: Player) : PlayerEvent() {
    companion object : EventCompanion<PlayerDespawn>
}