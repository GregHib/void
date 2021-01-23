package world.gregs.voidps.world.interact.entity.player.spawn

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerEvent
import world.gregs.voidps.engine.event.EventCompanion

data class PlayerDespawn(override val player: Player) : PlayerEvent() {
    companion object : EventCompanion<PlayerDespawn>
}