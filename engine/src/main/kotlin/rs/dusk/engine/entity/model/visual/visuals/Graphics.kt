package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.Graphic
import rs.dusk.engine.entity.model.visual.Visual
import rs.dusk.engine.entity.model.visual.VisualCompanion

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
class Graphics(val graphics: Array<Graphic>) : Visual {
    companion object : VisualCompanion<Graphics>()
}