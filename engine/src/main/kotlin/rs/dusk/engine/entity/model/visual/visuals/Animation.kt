package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Animation(
    var first: Int = -1,
    var second: Int = -1,
    var third: Int = -1,
    var fourth: Int = -1,
    var speed: Int = 0
) : Visual