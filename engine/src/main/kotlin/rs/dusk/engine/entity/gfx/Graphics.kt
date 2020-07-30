package rs.dusk.engine.entity.gfx

import rs.dusk.engine.entity.list.BatchList
import rs.dusk.engine.map.chunk.Chunk

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 04, 2020
 */
class Graphics(override val chunks: MutableMap<Chunk, MutableSet<AreaGraphic>> = mutableMapOf()) :
    BatchList<AreaGraphic>