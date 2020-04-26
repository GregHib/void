package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.visual.Visual

/**
 * Changes the characteristics to match NPC with [id]
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class Transform(
    var id: Int = -1
) : Visual