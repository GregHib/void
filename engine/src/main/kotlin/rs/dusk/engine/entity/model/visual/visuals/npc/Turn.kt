package rs.dusk.engine.entity.model.visual.visuals.npc

import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Turn(
    val x: Int = 0,
    val y: Int = 0,
    val directionX: Int = 0,
    val directionY: Int = 0
) : Visual