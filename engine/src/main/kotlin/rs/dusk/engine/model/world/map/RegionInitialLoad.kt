package rs.dusk.engine.model.world.map

import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.engine.model.entity.character.player.PlayerEvent

data class RegionInitialLoad(override val player: Player) : PlayerEvent() {
    companion object : EventCompanion<RegionInitialLoad>
}