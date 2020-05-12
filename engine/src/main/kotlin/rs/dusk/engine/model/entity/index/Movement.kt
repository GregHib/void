package rs.dusk.engine.model.entity.index

import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 26, 2020
 */
data class Movement(
    var lastTile: Tile = Tile(0),
    var delta: Tile = Tile(0),
    var direction: Int = -1
)