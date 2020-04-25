package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.visual.Visual
import rs.dusk.engine.entity.model.visual.VisualCompanion

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Animation(val first: Int, val second: Int, val third: Int, val fourth: Int, val speed: Int) : Visual {
    companion object : VisualCompanion<Animation>()
}