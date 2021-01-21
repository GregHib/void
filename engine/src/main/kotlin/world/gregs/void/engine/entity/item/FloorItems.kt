package world.gregs.void.engine.entity.item

import world.gregs.void.engine.entity.list.BatchList
import world.gregs.void.engine.map.Tile
import world.gregs.void.engine.map.chunk.Chunk

/**
 * @author GregHib <greg@gregs.world>
 * @since March 30, 2020
 */
class FloorItems(override val chunks: MutableMap<Chunk, MutableSet<FloorItem>> = mutableMapOf()) :
    BatchList<FloorItem>

fun Tile.offset(bit: Int = 4) = (x.rem(8) shl bit) or y.rem(8)