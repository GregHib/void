package rs.dusk.engine.path

import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 18, 2020
 */
interface ObstructionStrategy {
    fun obstructed(tile: Tile, direction: Direction): Boolean
}