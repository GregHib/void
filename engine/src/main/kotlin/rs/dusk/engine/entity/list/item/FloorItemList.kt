package rs.dusk.engine.entity.list.item

import com.google.common.collect.HashMultimap
import com.google.common.collect.SetMultimap
import rs.dusk.engine.entity.model.FloorItem
import rs.dusk.engine.model.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
class FloorItemList : FloorItems {
    override val delegate: SetMultimap<Tile, FloorItem> = HashMultimap.create()
}