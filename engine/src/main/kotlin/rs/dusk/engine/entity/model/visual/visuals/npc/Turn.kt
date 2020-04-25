package rs.dusk.engine.entity.model.visual.visuals.npc

import rs.dusk.engine.entity.model.visual.Visual
import rs.dusk.engine.entity.model.visual.VisualCompanion

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Turn(val x: Int, val y: Int, val directionX: Int, val directionY: Int) : Visual {
    companion object : VisualCompanion<Turn>()
}