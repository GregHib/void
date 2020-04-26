package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.visual.Visual
import rs.dusk.engine.model.Direction
import rs.dusk.engine.model.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class ForceMovement(
    var tile1: Tile,
    var delay1: Int = 0,
    var tile2: Tile? = null,
    var delay2: Int = 0,
    var direction: Direction = Direction.NONE
) : Visual