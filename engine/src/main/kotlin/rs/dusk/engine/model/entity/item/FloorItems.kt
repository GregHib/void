package rs.dusk.engine.model.entity.item

import rs.dusk.engine.model.entity.list.SimpleList

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
data class FloorItems(override val delegate: HashMap<Int, MutableSet<FloorItem>> = hashMapOf()) :
    SimpleList<FloorItem>