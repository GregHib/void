package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.visual.Visual
import rs.dusk.engine.entity.model.visual.VisualCompanion

/**
 * Changes the characteristics to match NPC with [id]
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Transform(val id: Int) : Visual {
    companion object : VisualCompanion<Transform>()
}