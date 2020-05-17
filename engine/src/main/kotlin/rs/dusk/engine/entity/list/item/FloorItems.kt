package rs.dusk.engine.entity.list.item

import rs.dusk.engine.entity.list.SimpleList
import rs.dusk.engine.model.entity.item.FloorItem

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
data class FloorItems(override val delegate: HashMap<Int, MutableSet<FloorItem>> = hashMapOf()) : SimpleList<FloorItem>