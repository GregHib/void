package world.gregs.voidps.engine.map.region

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerEvent
import world.gregs.voidps.engine.event.EventCompanion

data class RegionLogin(override val player: Player) : PlayerEvent() {
    companion object : EventCompanion<RegionLogin>
}