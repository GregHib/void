package rs.dusk.engine.model.map.region

import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.engine.model.entity.character.player.PlayerEvent

data class RegionLogin(override val player: Player) : PlayerEvent() {
    companion object : EventCompanion<RegionLogin>
}