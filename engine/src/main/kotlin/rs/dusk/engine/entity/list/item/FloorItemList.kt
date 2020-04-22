package rs.dusk.engine.entity.list.item

import rs.dusk.engine.entity.model.FloorItem

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
data class FloorItemList(override val delegate: HashMap<Int, MutableSet<FloorItem>> = hashMapOf()) : FloorItems