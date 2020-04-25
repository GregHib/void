package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.visual.Visual
import rs.dusk.engine.model.Direction
import rs.dusk.engine.model.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class ForceMovement(val tile1: Tile, val delay1: Int, val tile2: Tile, val delay2: Int, val direction: Direction) :
    Visual