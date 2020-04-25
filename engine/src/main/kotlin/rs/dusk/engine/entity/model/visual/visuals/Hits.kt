package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.Hit
import rs.dusk.engine.entity.model.visual.Visual
import rs.dusk.engine.entity.model.visual.VisualCompanion

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Hits(val hits: List<Hit>, val source: Int, val target: Int) : Visual {
    companion object : VisualCompanion<Hits>()
}