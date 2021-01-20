package world.gregs.void.engine.map.region

import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.player.PlayerEvent
import world.gregs.void.engine.event.EventCompanion

data class RegionLogin(override val player: Player) : PlayerEvent() {
    companion object : EventCompanion<RegionLogin>
}