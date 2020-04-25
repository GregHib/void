package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.visual.Visual
import rs.dusk.engine.entity.model.visual.VisualCompanion

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class ColourOverlay(val delay: Int, val duration: Int, val colour: Int) : Visual {
    companion object : VisualCompanion<ColourOverlay>()
}