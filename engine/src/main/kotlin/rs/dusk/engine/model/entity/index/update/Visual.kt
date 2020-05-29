package rs.dusk.engine.model.entity.index.update

import rs.dusk.engine.model.entity.index.Indexed

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
interface Visual {
    /**
     * Optional reset to be performed at the end of an update
     */
    fun reset(indexed: Indexed) {
    }
}