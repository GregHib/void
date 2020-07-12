package rs.dusk.world.entity.player.map

import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerEvent

data class RegionInitialLoad(override val player: Player) : PlayerEvent() {
    companion object : EventCompanion<RegionInitialLoad>
}