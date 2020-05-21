package rs.dusk.engine.model.entity.obj

import rs.dusk.engine.model.entity.list.SimpleList

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
data class Objects(override val delegate: HashMap<Int, MutableSet<Location>> = hashMapOf()) :
    SimpleList<Location>