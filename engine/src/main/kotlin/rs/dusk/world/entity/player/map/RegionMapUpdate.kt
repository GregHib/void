package rs.dusk.world.entity.player.map

import rs.dusk.engine.event.EventCompanion
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerEvent

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since June 21, 2020
 */
data class RegionMapUpdate(override val player: Player, val initial: Boolean) : PlayerEvent() {
    companion object : EventCompanion<RegionMapUpdate>
}