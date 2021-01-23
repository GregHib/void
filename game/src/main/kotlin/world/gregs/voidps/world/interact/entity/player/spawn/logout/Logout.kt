package world.gregs.voidps.world.interact.entity.player.spawn.logout

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerEvent
import world.gregs.voidps.engine.event.EventCompanion

data class Logout(override val player: Player) : PlayerEvent() {
    companion object : EventCompanion<Logout>
}