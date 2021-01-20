package world.gregs.void.engine.entity.gfx

import world.gregs.void.engine.entity.list.BatchList
import world.gregs.void.engine.map.chunk.Chunk

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 04, 2020
 */
class Graphics(override val chunks: MutableMap<Chunk, MutableSet<AreaGraphic>> = mutableMapOf()) :
    BatchList<AreaGraphic>