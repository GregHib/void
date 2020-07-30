package rs.dusk.engine.model.entity.item

import rs.dusk.engine.model.entity.list.BatchList
import rs.dusk.engine.model.map.Tile
import rs.dusk.engine.model.map.chunk.Chunk

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since March 30, 2020
 */
class FloorItems(override val chunks: MutableMap<Chunk, MutableSet<FloorItem>> = mutableMapOf()) :
    BatchList<FloorItem>

fun Tile.offset(bit: Int = 4) = (x.rem(8) shl bit) or y.rem(8)