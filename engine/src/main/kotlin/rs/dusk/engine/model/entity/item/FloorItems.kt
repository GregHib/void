package rs.dusk.engine.model.entity.item

import rs.dusk.engine.model.entity.list.BatchList
import rs.dusk.engine.model.world.ChunkPlane
import rs.dusk.engine.model.world.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
class FloorItems(override val chunks: MutableMap<ChunkPlane, MutableSet<FloorItem>> = mutableMapOf()) :
    BatchList<FloorItem>

fun Tile.offset(bit: Int = 4) = (x.rem(8) shl bit) or y.rem(8)