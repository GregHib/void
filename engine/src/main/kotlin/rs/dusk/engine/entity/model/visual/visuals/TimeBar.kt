package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class TimeBar(
    val full: Boolean = false,
    val exponentialDelay: Int = 0,
    val delay: Int = 0,
    val increment: Int = 0
) : Visual