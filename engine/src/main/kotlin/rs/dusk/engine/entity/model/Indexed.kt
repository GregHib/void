package rs.dusk.engine.entity.model

import rs.dusk.engine.entity.model.visual.Visuals

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
interface Indexed {
    val index: Int
    val visuals: Visuals
    val movement: Movement
    val changes: Changes
}