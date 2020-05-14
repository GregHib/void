package rs.dusk.engine.model.entity.index

import rs.dusk.engine.model.entity.Entity
import rs.dusk.engine.model.entity.index.update.Visuals

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
interface Indexed : Entity, Comparable<Indexed> {
    val index: Int
    val visuals: Visuals
    var change: LocalChange?
    val movement: Movement

    override fun compareTo(other: Indexed): Int {
        return index.compareTo(other.index)
    }
}