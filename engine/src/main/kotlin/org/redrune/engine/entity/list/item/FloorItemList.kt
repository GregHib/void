package org.redrune.engine.entity.list.item

import com.google.common.collect.HashMultimap
import com.google.common.collect.SetMultimap
import org.redrune.engine.entity.model.FloorItem
import org.redrune.engine.model.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 28, 2020
 */
class FloorItemList : FloorItems {
    override val delegate: SetMultimap<Tile, FloorItem> = HashMultimap.create()
}