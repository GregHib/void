package rs.dusk.engine.map.region

import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerEvent
import rs.dusk.engine.event.EventCompanion

data class RegionLogin(override val player: Player) : PlayerEvent() {
    companion object : EventCompanion<RegionLogin>
}