package rs.dusk.engine.model.entity.index

import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 26, 2020
 */
data class Movement(
    var lastTile: Tile = Tile.EMPTY,
    var delta: Tile = Tile.EMPTY,
    var walkStep: Direction = Direction.NONE,
    var runStep: Direction = Direction.NONE
) {
    fun reset() {
        delta = Tile.EMPTY
        walkStep = Direction.NONE
        runStep = Direction.NONE
    }
}