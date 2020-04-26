package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.Hit
import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Hits(
    val hits: MutableList<Hit> = mutableListOf(),
    var source: Int = -1,
    var target: Int = -1
) : Visual