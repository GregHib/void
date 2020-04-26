package rs.dusk.engine.entity.model

import rs.dusk.engine.model.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 26, 2020
 */
data class Movement(
    var lastTile: Tile = Tile(0),
    var delta: Tile = Tile(0),
    var direction: Int = -1,
    var run: Boolean = false,
    val type: Int = 0
)